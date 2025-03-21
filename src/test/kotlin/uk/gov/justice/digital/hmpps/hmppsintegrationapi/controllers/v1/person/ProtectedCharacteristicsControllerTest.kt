package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.CrnSupplier
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonProtectedCharacteristics
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetProtectedCharacteristicsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [ProtectedCharacteristicsController::class])
@ActiveProfiles("test")
internal class ProtectedCharacteristicsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getProtectedCharacteristicsService: GetProtectedCharacteristicsService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val crnSupplier: CrnSupplier,
  @MockitoBean val getCaseAccess: GetCaseAccess,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val filters = null
      val path = "/v1/persons/$hmppsId/protected-characteristics"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      describe("GET $path") {
        beforeTest {
          Mockito.reset(getProtectedCharacteristicsService)
          Mockito.reset(auditService)
          whenever(getProtectedCharacteristicsService.execute(hmppsId, filters)).thenReturn(
            Response(
              data =
                PersonProtectedCharacteristics(35, "Female", "Unknown", "British", "British", "None", emptyList()),
            ),
          )
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("gets the offences for a person with the matching ID") {
          mockMvc.performAuthorised(path)

          verify(getProtectedCharacteristicsService, VerificationModeFactory.times(1)).execute(hmppsId, filters)
        }

        it("returns the offences for a person with the matching ID") {
          val result = mockMvc.performAuthorised(path)

          result.response.contentAsString.shouldContain(
            """
          "data":
            {
              "age": 35,
              "gender": "Female",
              "sexualOrientation": "Unknown",
              "ethnicity": "British",
              "nationality": "British",
              "religion": "None",
              "disabilities": [],
              "maritalStatus": null,
              "reasonableAdjustments": [
              ]
             }

        """.removeWhitespaceAndNewlines(),
          )
        }
        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_PROTECTED_CHARACTERISTICS", mapOf("hmppsId" to hmppsId))
        }

        it("returns an empty list embedded in a JSON object when no offences are found") {
          val hmppsIdForPersonWithNoOffences = "A1234AA"
          val offencesPath = "/v1/persons/$hmppsIdForPersonWithNoOffences/protected-characteristics"

          whenever(getProtectedCharacteristicsService.execute(hmppsIdForPersonWithNoOffences, filters)).thenReturn(
            Response(
              data = null,
            ),
          )

          val result = mockMvc.performAuthorised(offencesPath)

          result.response.contentAsString.shouldContain("\"data\":null".removeWhitespaceAndNewlines())
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getProtectedCharacteristicsService.execute(hmppsId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NOMIS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("fails with the appropriate error when an upstream service is down") {
          whenever(getProtectedCharacteristicsService.execute(hmppsId, filters)).doThrow(
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
      }
    },
  )
