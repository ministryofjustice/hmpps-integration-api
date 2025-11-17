package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.assertions.json.shouldContainJsonKeyValue
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
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebMvcTestConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.StatusInformation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.REDACTION_MASKING_TEXT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithPndAlerts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetStatusInformationForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [StatusInformationController::class])
@Import(value = [WebMvcTestConfiguration::class])
@ActiveProfiles("test-redaction-enabled")
internal class StatusInformationControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getStatusInformationForPersonService: GetStatusInformationForPersonService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getCaseAccess: GetCaseAccess,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
) : DescribeSpec(
    {
      val hmppsId = "A8888AA"
      val path = "/v1/persons/$hmppsId/status-information"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val laoOkCrn = "R654321"
      val laoFailureCrn = "R754321"

      describe("GET $path") {
        beforeTest {
          mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
          every { roles["full-access"] } returns testRoleWithPndAlerts
          Mockito.reset(getStatusInformationForPersonService)
          Mockito.reset(auditService)
          whenever(getCaseAccess.getAccessFor(any())).thenReturn(CaseAccess(laoOkCrn, false, false, "", ""))
          whenever(getCaseAccess.getAccessFor("R754321")).thenReturn(null)
          whenever(getStatusInformationForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data =
                listOf(
                  StatusInformation(
                    code = "WRSM",
                    description = "Warrant/Summons - Outstanding warrant or summons",
                    startDate = "2022-09-01",
                    reviewDate = "2024-12-23",
                    notes = "A lot of notes",
                  ),
                ),
            ),
          )
        }

        afterTest {
          unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_STATUS_INFORMATION", mapOf("hmppsId" to hmppsId))
        }

        it("gets the status information for a person with the matching ID") {
          mockMvc.performAuthorised(path)

          verify(getStatusInformationForPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
        }

        it("returns the status information for a person with the matching ID") {
          val result = mockMvc.performAuthorised(path)

          result.response.contentAsString.shouldContain(
            """
          "data": [
            {
              "code": "WRSM",
              "description": "Warrant/Summons - Outstanding warrant or summons",
              "startDate": "2022-09-01",
              "reviewDate": "2024-12-23",
              "notes": "A lot of notes"
            }
          ]
        """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns the redacted status information for a person with the matching identifier") {
          val laoNoms = "S1234RE"
          val laoCrn = "S123456"
          whenever(getCaseAccess.getAccessFor(laoNoms)).thenReturn(CaseAccess(laoCrn, false, true, null, "Restriction Message"))
          whenever(getStatusInformationForPersonService.execute(laoNoms)).thenReturn(
            Response(
              data =
                listOf(
                  StatusInformation(
                    code = "WRSM",
                    description = "Warrant/Summons - Outstanding warrant or summons",
                    startDate = "2022-09-01",
                    reviewDate = "2024-12-23",
                    notes = "A lot of notes",
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised("/v1/persons/$laoNoms/status-information")

          result.response.contentAsString.shouldContain(
            """
          "data": [
            {
              "code": "WRSM",
              "description": "Warrant/Summons - Outstanding warrant or summons",
              "startDate": "2022-09-01",
              "reviewDate": "2024-12-23",
              "notes": "$REDACTION_MASKING_TEXT"
            }
          ]
        """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns an empty list when no status information found") {
          val hmppsIdForPersonWithNoStatusInformation = "A1234AA"
          val statusInformationPath = "/v1/persons/$hmppsIdForPersonWithNoStatusInformation/status-information"

          whenever(getStatusInformationForPersonService.execute(hmppsIdForPersonWithNoStatusInformation)).thenReturn(
            Response(
              data = emptyList(),
            ),
          )

          val result = mockMvc.performAuthorised(statusInformationPath)

          result.response.contentAsString.shouldContain("\"data\":[]".removeWhitespaceAndNewlines())
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getStatusInformationForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data = emptyList(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISON_API,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns paginated results") {
          whenever(getStatusInformationForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data =
                List(20) {
                  StatusInformation(
                    code = "XNR",
                    description = "Not For Release",
                    startDate = "2022-08-01",
                    reviewDate = "2025-08-01",
                    notes = "Notes all written here",
                  )
                },
            ),
          )

          val result = mockMvc.performAuthorised("$path?page=1&perPage=10")

          result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 1)
          result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 2)
        }

        it("fails with the appropriate error when an upstream service is down") {
          whenever(getStatusInformationForPersonService.execute(hmppsId)).doThrow(
            WebClientResponseException(500, "MockError", null, null, null, null),
          )

          val response = mockMvc.performAuthorised("$path?page=1&perPage=10")

          assert(response.response.status == 500)
          assert(
            response.response.contentAsString.equals(
              "{\"status\":500,\"errorCode\":null,\"userMessage\":\"500 MockError\",\"developerMessage\":\"Unable to complete request as an upstream service is not responding\",\"moreInfo\":null}",
            ),
          )
        }

        it("fails with the appropriate error when LAO context has failed to be retrieved") {
          whenever(getStatusInformationForPersonService.execute(laoFailureCrn)).thenReturn(
            Response(
              data =
                listOf(
                  StatusInformation(
                    code = "WRSM",
                    description = "Warrant/Summons - Outstanding warrant or summons",
                    startDate = "2022-09-01",
                    reviewDate = "2024-12-23",
                    notes = "A lot of notes",
                  ),
                ),
            ),
          )

          val response = mockMvc.performAuthorised("/v1/persons/$laoFailureCrn/status-information")

          assert(response.response.status == 500)
          assert(
            response.response.contentAsString.equals(
              "{\"status\":500,\"errorCode\":null,\"userMessage\":\"LAO Check failed\",\"developerMessage\":\"LAO Check failed\",\"moreInfo\":null}",
            ),
          )
        }

        it("fails with the appropriate error when role does not contain a WRSM status") {
          unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
          val response = mockMvc.performAuthorised("$path?page=1&perPage=10")
          val error = response.response.contentAsJson<ErrorResponse>()
          assertThat(error.status).isEqualTo(HttpStatus.FORBIDDEN.value())
          assertThat(error.developerMessage).contains("Consumer does not have status code WRSM configured")
        }
      }
    },
  )
