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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.*
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetOffencesForPersonService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

@WebMvcTest(controllers = [OffencesController::class])
internal class OffencesControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getOffencesForPersonService: GetOffencesForPersonService,
) : DescribeSpec(
  {
    val pncId = "9999/11111A"
    val encodedPncId = URLEncoder.encode(pncId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedPncId/offences"

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getOffencesForPersonService)
        whenever(getOffencesForPersonService.execute(pncId)).thenReturn(
          Response(
            data = listOf(
              Offence(
                date = LocalDate.parse("9999-01-01"),
                code = "RR99999",
                description = "This is a description of an offence.",
              ),
            ),
          ),
        )
      }

      it("responds with a 200 OK status") {
        val result = mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("retrieves the offences for a person with the matching ID") {
        mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        verify(getOffencesForPersonService, VerificationModeFactory.times(1)).execute(pncId)
      }

      it("returns the offences for a person with the matching ID") {
        val result = mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        result.response.contentAsString.shouldContain(
          """
          "data": [
            {
              "date": "9999-01-01",
              "code": "RR99999",
              "description": "This is a description of an offence."
            }
          ]
        """.removeWhitespaceAndNewlines(),
        )
      }

      it("returns an empty list embedded in a JSON object when no offences are found") {
        val pncIdForPersonWithNoOffences = "0000/11111A"
        val encodedPncIdForPersonWithNoOffences =
          URLEncoder.encode(pncIdForPersonWithNoOffences, StandardCharsets.UTF_8)
        val path = "/v1/persons/$encodedPncIdForPersonWithNoOffences/offences"

        whenever(getOffencesForPersonService.execute(pncIdForPersonWithNoOffences)).thenReturn(
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
        whenever(getOffencesForPersonService.execute(pncId)).thenReturn(
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
        whenever(getOffencesForPersonService.execute(pncId)).thenReturn(
          Response(
            data =
            List(20) {
              Offence(
                date = LocalDate.parse("9999-01-01"),
                code = "RR99999",
                description = "This is a description of an offence.",
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
