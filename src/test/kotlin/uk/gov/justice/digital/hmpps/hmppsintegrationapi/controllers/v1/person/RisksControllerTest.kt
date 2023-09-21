package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Risks
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetRisksForPersonService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

@WebMvcTest(controllers = [RisksController::class])
internal class RisksControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getRisksForPersonService: GetRisksForPersonService,
) : DescribeSpec(
  {
    val pncId = "9999/11111A"
    val encodedPncId = URLEncoder.encode(pncId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedPncId/risks"

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getRisksForPersonService)
        whenever(getRisksForPersonService.execute(pncId)).thenReturn(
          Response(
            data = Risks(
              assessedOn = LocalDateTime.of(
                2023,
                9,
                19,
                12,
                51,
                38,
              ),
            ),
          ),
        )
      }

      it("responds with a 200 OK status") {
        val result = mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("retrieves the risks for a person with the matching ID") {
        mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

        verify(getRisksForPersonService, VerificationModeFactory.times(1)).execute(pncId)
      }

      it("returns the risks for a person with the matching ID") {
        val result = mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

        result.response.contentAsString.shouldContain(
          """
          "data": {
               "assessedOn": "2023-09-19T12:51:38"
            }
          """.removeWhitespaceAndNewlines(),
        )
      }

      it("returns null embedded in a JSON object when no risks are found") {
        val pncIdForPersonWithNoRisks = "0000/11111A"
        val encodedPncIdForPersonWithNoRisks = URLEncoder.encode(pncIdForPersonWithNoRisks, StandardCharsets.UTF_8)
        val pathForPersonWithNoRisks = "/v1/persons/$encodedPncIdForPersonWithNoRisks/risks"

        whenever(getRisksForPersonService.execute(pncIdForPersonWithNoRisks)).thenReturn(Response(data = null))

        val result = mockMvc.perform(MockMvcRequestBuilders.get(pathForPersonWithNoRisks)).andReturn()

        result.response.contentAsString.shouldContain("\"data\":null")
      }

      it("responds with a 404 NOT FOUND status when person isn't found in the upstream API") {
        whenever(getRisksForPersonService.execute(pncId)).thenReturn(
          Response(
            data = null,
            errors = listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.ASSESS_RISKS_AND_NEEDS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
          ),
        )

        val result = mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }
    }
  },
)
