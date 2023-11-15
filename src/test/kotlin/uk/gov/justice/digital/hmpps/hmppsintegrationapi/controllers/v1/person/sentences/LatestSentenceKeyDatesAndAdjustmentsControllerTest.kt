package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person.sentences

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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.LatestSentenceKeyDatesAndAdjustments
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceAdjustment
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceKeyDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetLatestSentenceKeyDatesAndAdjustmentsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetSentencesForPersonService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

@WebMvcTest(controllers = [SentencesController::class])
internal class LatestSentenceKeyDatesAndAdjustmentsControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getSentencesForPersonService: GetSentencesForPersonService,
  @MockBean val getLatestSentenceKeyDatesAndAdjustmentsForPersonService: GetLatestSentenceKeyDatesAndAdjustmentsForPersonService,
) : DescribeSpec(
  {
    val hmppsId = "9999/11111A"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedHmppsId/sentences/latest-key-dates-and-adjustments"

    beforeTest {
      Mockito.reset(getLatestSentenceKeyDatesAndAdjustmentsForPersonService)
      whenever(getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId)).thenReturn(
        Response(
          data = LatestSentenceKeyDatesAndAdjustments(
            adjustments = SentenceAdjustment(
              additionalDaysAwarded = 7,
              unlawfullyAtLarge = 10,
              lawfullyAtLarge = 2,
              restoredAdditionalDaysAwarded = 0,
              specialRemission = 11,
              recallSentenceRemand = 1,
              recallSentenceTaggedBail = 3,
              remand = 6,
              taggedBail = 3,
              unusedRemand = 6,
            ),
            automaticRelease = SentenceKeyDate(date = LocalDate.parse("2023-04-01")),
          ),
        ),
      )
    }

    it("responds with a 200 OK status") {
      val result = mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

      result.response.status.shouldBe(HttpStatus.OK.value())
    }

    it("retrieves the latest sentence key dates and adjustments for a person with the matching ID") {
      mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

      verify(getLatestSentenceKeyDatesAndAdjustmentsForPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
    }

    it("returns the latest sentence key dates and adjustments for a person with the matching ID") {
      val result = mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

      result.response.contentAsString.shouldContain(
        """
          "data": {
            "adjustments": {
              "additionalDaysAwarded": 7,
              "unlawfullyAtLarge": 10,
              "lawfullyAtLarge": 2,
              "restoredAdditionalDaysAwarded": 0,
              "specialRemission": 11,
              "recallSentenceRemand": 1,
              "recallSentenceTaggedBail": 3,
              "remand": 6,
              "taggedBail": 3,
              "unusedRemand": 6
            },
            "automaticRelease": {
              "date": "2023-04-01"
            }
          }
          """.removeWhitespaceAndNewlines(),
      )
    }

    it("responds with a 404 NOT FOUND status when person isn't found in the upstream API") {
      whenever(getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId)).thenReturn(
        Response(
          data = null,
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
  },
)
