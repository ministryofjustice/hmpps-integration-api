package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebMvcTestConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MappaDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithLaoRedactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetMappaDetailForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [MappaDetailController::class])
@Import(value = [WebMvcTestConfiguration::class])
@ActiveProfiles("test-redaction-enabled")
internal class MappaDetailControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getMappaDetailForPersonService: GetMappaDetailForPersonService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getCaseAccess: GetCaseAccess,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val path = "/v1/persons/$hmppsId/risks/mappadetail"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val laoOkCrn = "R654321"
      val laoFailureCrn = "R754321"

      describe("GET $path") {
        beforeTest {
          mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
          every { roles["full-access"] } returns testRoleWithLaoRedactions
          Mockito.reset(getMappaDetailForPersonService)
          whenever(getCaseAccess.getAccessFor(any())).thenReturn(CaseAccess(laoOkCrn, false, false, "", ""))
          whenever(getCaseAccess.getAccessFor("R754321")).thenReturn(null)
          whenever(getMappaDetailForPersonService.execute(hmppsId)).thenReturn(
            Response(
              MappaDetail(
                level = 2,
                levelDescription = "A high level of risk",
                category = 3,
                categoryDescription = "Behaviour",
                startDate = "2024-03-08",
                reviewDate = "2024-10-08",
                notes = "Review in a week",
              ),
            ),
          )

          Mockito.reset(auditService)
        }

        afterTest {
          unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("gets the mappa detail for a person with the matching ID") {
          mockMvc.performAuthorised(path)
          verify(getMappaDetailForPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_MAPPA_DETAIL", mapOf("hmppsId" to hmppsId))
        }

        it("returns the risk categories for a person with the matching ID") {
          val result = mockMvc.performAuthorised(path)

          result.response.contentAsString.shouldContain(
            """
          "data": {
              "level": 2,
              "levelDescription": "A high level of risk",
              "category": 3,
              "categoryDescription": "Behaviour",
              "startDate": "2024-03-08",
              "reviewDate": "2024-10-08",
              "notes": "Review in a week"
          }
        """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns the redacted risk categories for a person with the matching identifier") {

          val laoCrn = "M123456"
          whenever(getCaseAccess.getAccessFor(laoCrn)).thenReturn(CaseAccess(laoCrn, true, true, "Exclusion Message", "Restriction Message"))
          whenever(getMappaDetailForPersonService.execute(laoCrn)).thenReturn(
            Response(
              MappaDetail(
                level = 2,
                levelDescription = "A high level of risk",
                category = 3,
                categoryDescription = "Behaviour",
                startDate = "2024-03-08",
                reviewDate = "2024-10-08",
                notes = "Review in a week",
              ),
            ),
          )

          val result = mockMvc.performAuthorised("/v1/persons/$laoCrn/risks/mappadetail")

          result.response.contentAsString.shouldContain(
            """
          "data": {
              "level": 2,
              "levelDescription": "A high level of risk",
              "category": 3,
              "categoryDescription": "Behaviour",
              "startDate": "2024-03-08",
              "reviewDate": "2024-10-08",
              "notes": "**REDACTED**"
          }
        """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns a 404 NOT FOUND status code when person isn't found in delius") {
          whenever(getMappaDetailForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data = MappaDetail(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NDELIUS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns a 404 NOT FOUND status code when mappadetails response is empty") {
          whenever(getMappaDetailForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data = MappaDetail(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NDELIUS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns a 400 NOT FOUND status code when person isn't found") {
          whenever(getMappaDetailForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISON_API,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }

        it("fails with the appropriate error when LAO context has failed to be retrieved") {
          whenever(getMappaDetailForPersonService.execute(laoFailureCrn)).thenReturn(
            Response(
              MappaDetail(
                level = 2,
                levelDescription = "A high level of risk",
                category = 3,
                categoryDescription = "Behaviour",
                startDate = "2024-03-08",
                reviewDate = "2024-10-08",
                notes = "Review in a week",
              ),
            ),
          )

          val response = mockMvc.performAuthorised("/v1/persons/$laoFailureCrn/risks/mappadetail")

          assert(response.response.status == 500)
          assert(
            response.response.contentAsString.equals(
              "{\"status\":500,\"errorCode\":null,\"userMessage\":\"LAO Check failed\",\"developerMessage\":\"LAO Check failed\",\"moreInfo\":null}",
            ),
          )
        }

        it("fails with the appropriate error when role does not contain a MAPPA status") {
          unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
          val response = mockMvc.performAuthorised("$path?page=1&perPage=10")
          val error = response.response.contentAsJson<ErrorResponse>()
          assertThat(error.status).isEqualTo(HttpStatus.FORBIDDEN.value())
          assertThat(error.developerMessage).contains("Consumer does not have status code MAPP configured")
        }
      }
    },
  )
