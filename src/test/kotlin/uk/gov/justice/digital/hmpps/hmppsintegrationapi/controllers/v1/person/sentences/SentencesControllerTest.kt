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
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person.SentencesController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LatestSentenceKeyDatesAndAdjustments
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetLatestSentenceKeyDatesAndAdjustmentsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetSentencesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDate

@WebMvcTest(controllers = [SentencesController::class])
@ActiveProfiles("test")
internal class SentencesControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getSentencesForPersonService: GetSentencesForPersonService,
  @MockitoBean val getLatestSentenceKeyDatesAndAdjustmentsForPersonService: GetLatestSentenceKeyDatesAndAdjustmentsForPersonService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      describe("/sentences") {
        val hmppsId = "A1234AA"
        val path = "/v1/persons/$hmppsId/sentences"
        val filters = null

        beforeTest {
          Mockito.reset(getSentencesForPersonService)
          whenever(getSentencesForPersonService.execute(hmppsId, filters)).thenReturn(
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

          verify(getSentencesForPersonService, VerificationModeFactory.times(1)).execute(hmppsId, filters)
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
          val hmppsIdForPersonWithNoSentences = "A1234AA"
          val sentencesPath = "/v1/persons/$hmppsIdForPersonWithNoSentences/sentences"

          whenever(getSentencesForPersonService.execute(hmppsIdForPersonWithNoSentences, filters)).thenReturn(
            Response(
              data = emptyList(),
            ),
          )

          val result = mockMvc.performAuthorised(sentencesPath)

          result.response.contentAsString.shouldContain("\"data\":[]".removeWhitespaceAndNewlines())
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getSentencesForPersonService.execute(hmppsId, filters)).thenReturn(
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
          whenever(getSentencesForPersonService.execute(hmppsId, filters)).thenReturn(
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
          whenever(getSentencesForPersonService.execute(hmppsId, filters)).thenReturn(
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
          whenever(getSentencesForPersonService.execute(hmppsId, filters)).doThrow(
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
      }

      describe("/sentences/latest-key-dates-and-adjustments") {
        val hmppsId = "G2996UX"
        val path = "/v1/persons/$hmppsId/sentences/latest-key-dates-and-adjustments"

        beforeTest {
          Mockito.reset(getLatestSentenceKeyDatesAndAdjustmentsForPersonService)
          Mockito.reset(auditService)

          whenever(getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId, filters = null)).thenReturn(
            Response(
              data =
                LatestSentenceKeyDatesAndAdjustments(actualParoleDate = LocalDate.parse("2024-01-01")),
            ),
          )
        }

        it("returns a 200 OK status code with data") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsString.shouldContainJsonKeyValue("$.data.actualParoleDate", "2024-01-01")
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_SENTENCES_LATEST_KEY_DATES_AND_ADJUSTMENTS", mapOf("hmppsId" to hmppsId))
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId, filters = null)).thenReturn(
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

        it("returns a 400 bad request status code when error returned from upstream API") {
          whenever(getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId, filters = null)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NOMIS,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }

        it("returns a 500 INTERNAL SERVER ERROR status code when exception thrown by service") {
          whenever(getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId, filters = null)).thenThrow(
            IllegalStateException("Error occurred in upstream API"),
          )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.INTERNAL_SERVER_ERROR.value())
        }
      }
    },
  )
