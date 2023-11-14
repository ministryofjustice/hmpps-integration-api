package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alert
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAlertsForPersonService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

@WebMvcTest(controllers = [AlertsController::class])
internal class AlertsControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getAlertsForPersonService: GetAlertsForPersonService,
) : DescribeSpec(
  {
    val hmppsId = "9999/11111A"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedHmppsId/alerts"

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getAlertsForPersonService)
        whenever(getAlertsForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data = listOf(
              Alert(
                offenderNo = "A7777ZZ",
                type = "X",
                typeDescription = "Security",
                code = "XNR",
                codeDescription = "Not For Release",
                comment = "IS91",
                dateCreated = LocalDate.parse("2022-08-01"),
                dateExpired = LocalDate.parse("2023-08-01"),
                expired = false,
                active = true,
              ),
            ),
          ),
        )
      }

      it("responds with a 200 OK status") {
        val result = mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("retrieves the alerts for a person with the matching ID") {
        mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        verify(getAlertsForPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
      }

      it("returns the alerts for a person with the matching ID") {
        val result = mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        result.response.contentAsString.shouldContain(
          """
          "data": [
            {
              "offenderNo": "A7777ZZ",
              "type": "X",
              "typeDescription": "Security",
              "code": "XNR",
              "codeDescription": "Not For Release",
              "comment": "IS91",
              "dateCreated": "2022-08-01",
              "dateExpired": "2023-08-01",
              "expired": false,
              "active": true
            }
          ]
        """.removeWhitespaceAndNewlines(),
        )
      }

      it("returns an empty list embedded in a JSON object when no alerts are found") {
        val hmppsIdForPersonWithNoAlerts = "0000/11111A"
        val encodedHmppsIdForPersonWithNoAlerts =
          URLEncoder.encode(hmppsIdForPersonWithNoAlerts, StandardCharsets.UTF_8)
        val path = "/v1/persons/$encodedHmppsIdForPersonWithNoAlerts/alerts"

        whenever(getAlertsForPersonService.execute(hmppsIdForPersonWithNoAlerts)).thenReturn(
          Response(
            data = emptyList(),
          ),
        )

        val result =
          mockMvc.perform(MockMvcRequestBuilders.get("$path"))
            .andReturn()

        result.response.contentAsString.shouldContain("\"data\":[]".removeWhitespaceAndNewlines())
      }

      it("responds with a 404 NOT FOUND status when person isn't found in the upstream API") {
        whenever(getAlertsForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors = listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.NOMIS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
          ),
        )

        val result = mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("returns paginated results") {
        whenever(getAlertsForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data =
            List(20) {
              Alert(
                offenderNo = "A7777ZZ",
                type = "X",
                typeDescription = "Security",
                code = "XNR",
                codeDescription = "Not For Release",
                comment = "IS91",
                dateCreated = LocalDate.parse("2022-08-01"),
                dateExpired = LocalDate.parse("2023-08-01"),
                expired = false,
                active = true,
              )
            },
          ),
        )

        val result =
          mockMvc.perform(MockMvcRequestBuilders.get("$path?page=1&perPage=10"))
            .andReturn()

        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 1)
        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 2)
      }
    }
  },
)
