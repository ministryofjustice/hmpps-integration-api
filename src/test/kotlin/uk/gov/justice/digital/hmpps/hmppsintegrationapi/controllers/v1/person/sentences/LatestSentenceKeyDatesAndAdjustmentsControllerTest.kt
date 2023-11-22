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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.HomeDetentionCurfewDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.LatestSentenceKeyDatesAndAdjustments
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.NonDtoDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ReleaseDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceAdjustment
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceDate
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
            automaticRelease = SentenceKeyDate(date = LocalDate.parse("2023-04-01"), overrideDate = LocalDate.parse("2023-04-01")),
            conditionalRelease = SentenceKeyDate(date = LocalDate.parse("2023-05-01"), overrideDate = LocalDate.parse("2023-05-01")),
            dtoPostRecallRelease = SentenceKeyDate(date = LocalDate.parse("2024-01-02"), overrideDate = LocalDate.parse("2024-01-02")),
            earlyTerm = SentenceKeyDate(date = LocalDate.parse("2021-11-02"), overrideDate = LocalDate.parse("2021-11-02"), calculatedDate = LocalDate.parse("2021-11-02")),
            homeDetentionCurfew = HomeDetentionCurfewDate(
              actualDate = LocalDate.parse("2022-04-01"),
              eligibilityDate = LocalDate.parse("2022-04-01"),
              eligibilityCalculatedDate = LocalDate.parse("2022-04-01"),
              eligibilityOverrideDate = LocalDate.parse("2022-04-01"),
              endDate = LocalDate.parse("2022-04-01"),
            ),
            lateTerm = SentenceKeyDate(date = LocalDate.parse("2022-01-01"), overrideDate = LocalDate.parse("2022-01-01"), calculatedDate = LocalDate.parse("2022-01-01")),
            licenceExpiry = SentenceKeyDate(date = LocalDate.parse("2025-02-01"), overrideDate = LocalDate.parse("2025-02-01"), calculatedDate = LocalDate.parse("2025-02-01")),
            midTerm = SentenceKeyDate(date = LocalDate.parse("2024-02-01"), overrideDate = LocalDate.parse("2024-02-01"), calculatedDate = LocalDate.parse("2024-02-01")),
            nonDto = NonDtoDate(date = LocalDate.parse("2024-02-01"), releaseDateType = "CRD"),
            nonParole = SentenceKeyDate(date = LocalDate.parse("2026-11-02"), overrideDate = LocalDate.parse("2026-11-02")),
            paroleEligibility = SentenceKeyDate(date = LocalDate.parse("2027-02-01"), overrideDate = LocalDate.parse("2027-02-01"), calculatedDate = LocalDate.parse("2027-02-01")),
            postRecallRelease = SentenceKeyDate(date = LocalDate.parse("2028-02-01"), overrideDate = LocalDate.parse("2028-02-01")),
            release = ReleaseDate(date = LocalDate.parse("2030-02-01"), confirmedDate = LocalDate.parse("2030-02-01")),
            sentence = SentenceDate(
              effectiveEndDate = LocalDate.parse("2025-02-01"),
              expiryCalculatedDate = LocalDate.parse("2025-02-01"),
              expiryDate = LocalDate.parse("2025-02-01"),
              expiryOverrideDate = LocalDate.parse("2025-02-01"),
              startDate = LocalDate.parse("2025-02-01"),
            ),
            actualParoleDate = LocalDate.parse("2031-02-01"),
            earlyRemovalSchemeEligibilityDate = LocalDate.parse("2031-02-01"),
            releaseOnTemporaryLicenceDate = LocalDate.parse("2031-02-01"),
            tariffDate = LocalDate.parse("2031-02-01"),
            tariffEarlyRemovalSchemeEligibilityDate = LocalDate.parse("2031-02-01"),
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
        {
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
                "date": "2023-04-01",
                "overrideDate": "2023-04-01",
                "calculatedDate": null
            },
            "conditionalRelease": {
                "date": "2023-05-01",
                "overrideDate": "2023-05-01",
                "calculatedDate": null
            },
            "dtoPostRecallRelease": {
                "date": "2024-01-02",
                "overrideDate": "2024-01-02",
                "calculatedDate": null
            },
            "earlyTerm": {
                "date": "2021-11-02",
                "overrideDate": "2021-11-02",
                "calculatedDate": "2021-11-02"
            },
            "homeDetentionCurfew": {
                "actualDate": "2022-04-01",
                "eligibilityCalculatedDate": "2022-04-01",
                "eligibilityDate": "2022-04-01",
                "eligibilityOverrideDate": "2022-04-01",
                "endDate": "2022-04-01"
            },
            "lateTerm": {
                "date": "2022-01-01",
                "overrideDate": "2022-01-01",
                "calculatedDate": "2022-01-01"
            },
            "licenceExpiry": {
                "date": "2025-02-01",
                "overrideDate": "2025-02-01",
                "calculatedDate": "2025-02-01"
            },
            "midTerm": {
                "date": "2024-02-01",
                "overrideDate": "2024-02-01",
                "calculatedDate": "2024-02-01"
            },
            "nonDto": {
                "date": "2024-02-01",
                "releaseDateType": "CRD"
            },
            "nonParole": {
                "date": "2026-11-02",
                "overrideDate": "2026-11-02",
                "calculatedDate": null
            },
            "paroleEligibility": {
                "date": "2027-02-01",
                "overrideDate": "2027-02-01",
                "calculatedDate": "2027-02-01"
            },
            "postRecallRelease": {
                "date": "2028-02-01",
                "overrideDate": "2028-02-01",
                "calculatedDate": null
            },
            "release": {
                "date": "2030-02-01",
                "confirmedDate": "2030-02-01"
            },
            "sentence": {
              "effectiveEndDate": "2025-02-01",
              "expiryCalculatedDate": "2025-02-01",
              "expiryDate": "2025-02-01",
              "expiryOverrideDate": "2025-02-01",
              "startDate": "2025-02-01"
            },
            "actualParoleDate": "2031-02-01",
            "earlyRemovalSchemeEligibilityDate": "2031-02-01",
            "releaseOnTemporaryLicenceDate": "2031-02-01",
            "tariffDate": "2031-02-01",
            "tariffEarlyRemovalSchemeEligibilityDate": "2031-02-01"
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
