package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person.sentences

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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person.SentencesController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetLatestSentenceKeyDatesAndAdjustmentsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetSentencesForPersonService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

@WebMvcTest(controllers = [SentencesController::class])
internal class SentencesControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getSentencesForPersonService: GetSentencesForPersonService,
  @MockBean val getLatestSentenceKeyDatesAndAdjustmentsForPersonService: GetLatestSentenceKeyDatesAndAdjustmentsForPersonService,
) : DescribeSpec(
  {
    val hmppsId = "9999/11111A"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedHmppsId/sentences"

    beforeTest {
      Mockito.reset(getSentencesForPersonService)
      whenever(getSentencesForPersonService.execute(hmppsId)).thenReturn(
        Response(
          data = listOf(
            generateTestSentence(description = "Some description 1"),
            generateTestSentence(description = "Some description 2"),
          ),
        ),
      )
    }

    it("responds with a 200 OK status") {
      val result = mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

      result.response.status.shouldBe(HttpStatus.OK.value())
    }

    it("retrieves the sentences for a person with the matching ID") {
      mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

      verify(getSentencesForPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
    }

    it("returns the sentences for a person with the matching ID") {
      val result = mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

      result.response.contentAsString.shouldContain(
        """
          [
            {
              "dataSource": "NOMIS",
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
              "dataSource": "NOMIS",
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
      val path = "/v1/persons/$encodedHmppsIdForPersonWithNoSentences/sentences"

      whenever(getSentencesForPersonService.execute(hmppsIdForPersonWithNoSentences)).thenReturn(
        Response(
          data = emptyList(),
        ),
      )

      val result = mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

      result.response.contentAsString.shouldContain("\"data\":[]".removeWhitespaceAndNewlines())
    }

    it("responds with a 404 NOT FOUND status when person isn't found in the upstream API") {
      whenever(getSentencesForPersonService.execute(hmppsId)).thenReturn(
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

      val result = mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

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

      val result = mockMvc.perform(MockMvcRequestBuilders.get("$path?page=1&perPage=10")).andReturn()

      result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 1)
      result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 2)
    }
  },
)
