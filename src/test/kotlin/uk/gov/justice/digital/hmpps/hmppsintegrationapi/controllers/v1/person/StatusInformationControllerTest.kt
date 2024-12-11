package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.assertions.json.shouldContainJsonKeyValue
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.StatusInformation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetStatusInformationForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@WebMvcTest(controllers = [StatusInformationController::class])
@ActiveProfiles("test")
internal class StatusInformationControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getStatusInformationForPersonService: GetStatusInformationForPersonService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val hmppsId = "8888/12345P"
      val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
      val path = "/v1/persons/$encodedHmppsId/status-information"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      describe("GET $path") {
        beforeTest {
          Mockito.reset(getStatusInformationForPersonService)
          Mockito.reset(auditService)
          whenever(getStatusInformationForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data =
                listOf(
                  StatusInformation(
                    code = "ASFO",
                    description = "Serious Further Offence - Subject to SFO review/investigation",
                    startDate = "2022-10-18",
                    reviewDate = "2025-12-22",
                    notes = "To review later on in the year.",
                  ),
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
              "code": "ASFO",
              "description": "Serious Further Offence - Subject to SFO review/investigation",
              "startDate": "2022-10-18",
              "reviewDate": "2025-12-22",
              "notes": "To review later on in the year."
            },
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

        it("returns an empty list when no status information found") {
          val hmppsIdForPersonWithNoStatusInformation = "0999/12345B"
          val encodedHmppsIdForPersonWithNoStatusInformation =
            URLEncoder.encode(hmppsIdForPersonWithNoStatusInformation, StandardCharsets.UTF_8)
          val statusInformationPath = "/v1/persons/$encodedHmppsIdForPersonWithNoStatusInformation/status-information"

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
                    causedBy = UpstreamApi.NOMIS,
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
      }
    },
  )
