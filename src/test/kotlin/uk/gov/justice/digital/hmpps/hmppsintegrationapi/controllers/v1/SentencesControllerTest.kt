package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetSentencesForPersonService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence as IntegrationApiSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Term as IntegrationApiTerm

@WebMvcTest(controllers = [SentencesController::class])
internal class SentencesControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getSentencesForPersonService: GetSentencesForPersonService,
) : DescribeSpec(
  {
    val pncId = "9999/11111A"
    val encodedPncId = URLEncoder.encode(pncId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedPncId/sentences"

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getSentencesForPersonService)
        whenever(getSentencesForPersonService.execute(pncId)).thenReturn(
          Response(
            data = listOf(
              IntegrationApiSentence(
                dateOfSentencing = LocalDate.parse("1990-01-01"),
                description = "CJA - Community Order",
                isActive = true,
                terms = listOf(
                  IntegrationApiTerm(
                    hours = 2,
                  ),
                  IntegrationApiTerm(
                    years = 25,
                  ),
                ),
              ),
              IntegrationApiSentence(
                dateOfSentencing = LocalDate.parse("1991-01-01"),
                description = "ORA CJA03 Standard Determinate Sentence",
                isActive = false,
                terms = listOf(
                  IntegrationApiTerm(
                    years = 15,
                    months = 6,
                    weeks = 2,
                  ),
                  IntegrationApiTerm(
                    months = 6,
                    weeks = 2,
                    days = 5,
                  ),
                ),
              ),
              IntegrationApiSentence(
                dateOfSentencing = LocalDate.parse("1992-01-01"),
                description = "CJA - Suspended Sentence Order",
                isActive = null,
                terms = listOf(
                  IntegrationApiTerm(
                    years = 15,
                  ),
                  IntegrationApiTerm(
                    weeks = 2,
                  ),
                ),
              ),
            ),
          ),
        )
      }

      it("responds with a 200 OK status") {
        val result = mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("retrieves the sentences for a person with the matching ID") {
        mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        verify(getSentencesForPersonService, VerificationModeFactory.times(1)).execute(pncId)
      }

      it("returns the sentences for a person with the matching ID") {
        val result = mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

        result.response.contentAsString.shouldContain(
          """
          [
            {
                "dateOfSentencing": "1990-01-01",
                "description": "CJA - Community Order",
                "isActive": true,
                "terms": [
                    {
                        "years": null,
                        "months": null,
                        "weeks": null,
                        "days": null,
                        "hours": 2
                    },
                    {
                        "years": 25,
                        "months": null,
                        "weeks": null,
                        "days": null,
                        "hours": null
                    }
                ]
            },
            {
                "dateOfSentencing": "1991-01-01",
                "description": "ORA CJA03 Standard Determinate Sentence",
                "isActive": false,
                "terms": [
                    {
                        "years": 15,
                        "months": 6,
                        "weeks": 2,
                        "days": null,
                        "hours": null
                    },
                    {
                        "years": null,
                        "months": 6,
                        "weeks": 2,
                        "days": 5,
                        "hours": null
                    }
                ]
            },
            {
                "dateOfSentencing": "1992-01-01",
                "description": "CJA - Suspended Sentence Order",
                "isActive": null,
                "terms": [
                    {
                        "years": 15,
                        "months": null,
                        "weeks": null,
                        "days": null,
                        "hours": null
                    },
                    {
                        "years": null,
                        "months": null,
                        "weeks": 2,
                        "days": null,
                        "hours": null
                    }
                ]
            }
        ]
          """.removeWhitespaceAndNewlines(),
        )
      }

      it("returns an empty list embedded in a JSON object when no sentences are found") {
        val pncIdForPersonWithNoSentences = "0000/11111A"
        val encodedPncIdForPersonWithNoSentences =
          URLEncoder.encode(pncIdForPersonWithNoSentences, StandardCharsets.UTF_8)
        val path = "/v1/persons/$encodedPncIdForPersonWithNoSentences/sentences"

        whenever(getSentencesForPersonService.execute(pncIdForPersonWithNoSentences)).thenReturn(
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
        whenever(getSentencesForPersonService.execute(pncId)).thenReturn(
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
        whenever(getSentencesForPersonService.execute(pncId)).thenReturn(
          Response(
            data =
            List(20) {
              IntegrationApiSentence(
                dateOfSentencing = LocalDate.parse("2023-01-01"),
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
