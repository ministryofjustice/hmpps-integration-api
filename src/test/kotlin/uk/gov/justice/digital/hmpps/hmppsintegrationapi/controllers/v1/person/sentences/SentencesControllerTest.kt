package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person.sentences

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
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person.SentencesController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetLatestSentenceKeyDatesAndAdjustmentsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetSentencesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

@WebMvcTest(controllers = [SentencesController::class])
@ActiveProfiles("test")
internal class SentencesControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockBean val getSentencesForPersonService: GetSentencesForPersonService,
  @MockBean val getLatestSentenceKeyDatesAndAdjustmentsForPersonService: GetLatestSentenceKeyDatesAndAdjustmentsForPersonService,
  @MockBean val auditService: AuditService,
) : DescribeSpec(
    {
      val hmppsId = "9999/11111A"
      val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
      val path = "/v1/persons/$encodedHmppsId/sentences"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      beforeTest {
        Mockito.reset(getSentencesForPersonService)
        whenever(getSentencesForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data =
              listOf(
                generateTestSentence(description = "Some description 1"),
                generateTestSentence(description = "Some description 2"),
              ),
          ),
        )
        Mockito.reset(auditService)
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("gets the sentences for a person with the matching ID") {
        mockMvc.performAuthorised(path)

        verify(getSentencesForPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
      }

      it("returns the sentences for a person with the matching ID") {
        val result = mockMvc.performAuthorised(path)

        result.response.contentAsString.shouldContain(
          """
        [
          {
            "serviceSource": "NOMIS",
            "systemSource": "PRISON_SYSTEMS",
            "dateOfSentencing": null,
            "description": "Some description 1",
            "isActive": true,
            "isCustodial": true,
            "fineAmount": null,
            "length": {
                "duration": null,
                "units": null,
                "terms": [
                    {
                      "years": null,
                      "months": null,
                      "weeks": null,
                      "days": null,
                      "hours": 2,
                      "prisonTermCode": null
                    },
                    {
                      "years": 25,
                      "months": null,
                      "weeks": null,
                      "days": null,
                      "hours": null,
                      "prisonTermCode": null
                    }
                ]
            }
          },
          {
            "serviceSource": "NOMIS",
            "systemSource": "PRISON_SYSTEMS",
            "dateOfSentencing": null,
            "description": "Some description 2",
            "isActive": true,
            "isCustodial": true,
            "fineAmount": null,
            "length": {
                "duration": null,
                "units": null,
                "terms": [
                    {
                      "years": null,
                      "months": null,
                      "weeks": null,
                      "days": null,
                      "hours": 2,
                      "prisonTermCode": null
                    },
                    {
                      "years": 25,
                      "months": null,
                      "weeks": null,
                      "days": null,
                      "hours": null,
                      "prisonTermCode": null
                    }
                 ]
             }
          }
        ]
        """.removeWhitespaceAndNewlines(),
        )
      }

      it("returns an empty list embedded in a JSON object when no sentences are found") {
        val hmppsIdForPersonWithNoSentences = "0000/11111A"
        val encodedHmppsIdForPersonWithNoSentences =
          URLEncoder.encode(hmppsIdForPersonWithNoSentences, StandardCharsets.UTF_8)
        val sentencesPath = "/v1/persons/$encodedHmppsIdForPersonWithNoSentences/sentences"

        whenever(getSentencesForPersonService.execute(hmppsIdForPersonWithNoSentences)).thenReturn(
          Response(
            data = emptyList(),
          ),
        )

        val result = mockMvc.performAuthorised(sentencesPath)

        result.response.contentAsString.shouldContain("\"data\":[]".removeWhitespaceAndNewlines())
      }

      it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
        whenever(getSentencesForPersonService.execute(hmppsId)).thenReturn(
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
        whenever(getSentencesForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data =
              List(20) {
                generateTestSentence(
                  dateOfSentencing = LocalDate.parse("2023-01-01"),
                  isCustodial = true,
                )
              },
          ),
        )

        val result = mockMvc.performAuthorised("$path?page=1&perPage=10")
        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 1)
        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 2)
      }
      it("logs audit") {
        whenever(getSentencesForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data =
              List(20) {
                generateTestSentence(
                  dateOfSentencing = LocalDate.parse("2023-01-01"),
                  isCustodial = true,
                )
              },
          ),
        )
        mockMvc.performAuthorised("$path?page=1&perPage=10")

        verify(
          auditService,
          VerificationModeFactory.times(1),
        ).createEvent("GET_PERSON_SENTENCES", mapOf("hmppsId" to hmppsId))
      }

      it("returns a 500 INTERNAL SERVER ERROR status code when upstream api return expected error") {

        whenever(getSentencesForPersonService.execute(hmppsId)).doThrow(
          WebClientResponseException(500, "MockError", null, null, null, null),
        )

        val result = mockMvc.performAuthorised(path)
        assert(result.response.status == 500)
        assert(
          result.response.contentAsString.equals(
            "{\"status\":500,\"errorCode\":null,\"userMessage\":\"500 MockError\",\"developerMessage\":\"Unable to complete request as an upstream service is not responding\",\"moreInfo\":null}",
          ),
        )
      }
    },
  )
