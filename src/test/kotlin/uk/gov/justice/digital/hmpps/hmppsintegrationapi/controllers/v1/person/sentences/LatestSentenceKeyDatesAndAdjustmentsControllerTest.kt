package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person.sentences

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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.HmppsIdConverter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HomeDetentionCurfewDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LatestSentenceKeyDatesAndAdjustments
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NonDtoDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReleaseDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceAdjustment
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceKeyDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceKeyDateWithCalculatedDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TopupSupervision
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
internal class LatestSentenceKeyDatesAndAdjustmentsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getSentencesForPersonService: GetSentencesForPersonService,
  @MockitoBean val getLatestSentenceKeyDatesAndAdjustmentsForPersonService: GetLatestSentenceKeyDatesAndAdjustmentsForPersonService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val hmppsIdConverter: HmppsIdConverter,
  @MockitoBean val getCaseAccess: GetCaseAccess,
) : DescribeSpec(
    {
      val hmppsId = "9999/11111A"
      val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
      val path = "/v1/persons/$encodedHmppsId/sentences/latest-key-dates-and-adjustments"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      beforeTest {
        Mockito.reset(getLatestSentenceKeyDatesAndAdjustmentsForPersonService)
        whenever(getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId, null)).thenReturn(
          Response(
            data =
              LatestSentenceKeyDatesAndAdjustments(
                adjustments =
                  SentenceAdjustment(
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
                earlyTerm =
                  SentenceKeyDateWithCalculatedDate(
                    date = LocalDate.parse("2021-11-02"),
                    overrideDate = LocalDate.parse("2021-11-02"),
                    calculatedDate = LocalDate.parse("2021-11-02"),
                  ),
                homeDetentionCurfew =
                  HomeDetentionCurfewDate(
                    actualDate = LocalDate.parse("2022-04-01"),
                    eligibilityDate = LocalDate.parse("2022-04-01"),
                    eligibilityCalculatedDate = LocalDate.parse("2022-04-01"),
                    eligibilityOverrideDate = LocalDate.parse("2022-04-01"),
                    endDate = LocalDate.parse("2022-04-01"),
                  ),
                lateTerm =
                  SentenceKeyDateWithCalculatedDate(
                    date = LocalDate.parse("2022-01-01"),
                    overrideDate = LocalDate.parse("2022-01-01"),
                    calculatedDate = LocalDate.parse("2022-01-01"),
                  ),
                licenceExpiry =
                  SentenceKeyDateWithCalculatedDate(
                    date = LocalDate.parse("2025-02-01"),
                    overrideDate = LocalDate.parse("2025-02-01"),
                    calculatedDate = LocalDate.parse("2025-02-01"),
                  ),
                midTerm =
                  SentenceKeyDateWithCalculatedDate(
                    date = LocalDate.parse("2024-02-01"),
                    overrideDate = LocalDate.parse("2024-02-01"),
                    calculatedDate = LocalDate.parse("2024-02-01"),
                  ),
                nonDto = NonDtoDate(date = LocalDate.parse("2024-02-01"), releaseDateType = "CRD"),
                nonParole = SentenceKeyDate(date = LocalDate.parse("2026-11-02"), overrideDate = LocalDate.parse("2026-11-02")),
                paroleEligibility =
                  SentenceKeyDateWithCalculatedDate(
                    date = LocalDate.parse("2027-02-01"),
                    overrideDate = LocalDate.parse("2027-02-01"),
                    calculatedDate = LocalDate.parse("2027-02-01"),
                  ),
                postRecallRelease = SentenceKeyDate(date = LocalDate.parse("2028-02-01"), overrideDate = LocalDate.parse("2028-02-01")),
                release = ReleaseDate(date = LocalDate.parse("2030-02-01"), confirmedDate = LocalDate.parse("2030-02-01")),
                sentence =
                  SentenceDate(
                    effectiveEndDate = LocalDate.parse("2025-02-01"),
                    expiryCalculatedDate = LocalDate.parse("2025-02-01"),
                    expiryDate = LocalDate.parse("2025-02-01"),
                    expiryOverrideDate = LocalDate.parse("2025-02-01"),
                    startDate = LocalDate.parse("2025-02-01"),
                  ),
                topupSupervision =
                  TopupSupervision(
                    expiryCalculatedDate = LocalDate.parse("2022-04-01"),
                    expiryDate = LocalDate.parse("2022-04-01"),
                    expiryOverrideDate = LocalDate.parse("2022-04-01"),
                    startDate = LocalDate.parse("2022-04-01"),
                  ),
                actualParoleDate = LocalDate.parse("2031-02-01"),
                earlyRemovalSchemeEligibilityDate = LocalDate.parse("2031-02-01"),
                releaseOnTemporaryLicenceDate = LocalDate.parse("2031-02-01"),
                tariffDate = LocalDate.parse("2031-02-01"),
                tariffEarlyRemovalSchemeEligibilityDate = LocalDate.parse("2031-02-01"),
              ),
          ),
        )
        Mockito.reset(auditService)
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("gets the latest sentence key dates and adjustments for a person with the matching ID") {
        mockMvc.performAuthorised(path)

        verify(getLatestSentenceKeyDatesAndAdjustmentsForPersonService, VerificationModeFactory.times(1)).execute(hmppsId, null)
      }

      it("returns the latest sentence key dates and adjustments for a person with the matching ID") {
        val result = mockMvc.performAuthorised(path)

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
                    "overrideDate": "2023-04-01"
                },
                "conditionalRelease": {
                    "date": "2023-05-01",
                    "overrideDate": "2023-05-01"
                },
                "dtoPostRecallRelease": {
                    "date": "2024-01-02",
                    "overrideDate": "2024-01-02"
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
                    "overrideDate": "2026-11-02"
                },
                "paroleEligibility": {
                    "date": "2027-02-01",
                    "overrideDate": "2027-02-01",
                    "calculatedDate": "2027-02-01"
                },
                "postRecallRelease": {
                    "date": "2028-02-01",
                    "overrideDate": "2028-02-01"
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
                "topupSupervision": {
                    "expiryCalculatedDate": "2022-04-01",
                    "expiryDate": "2022-04-01",
                    "expiryOverrideDate": "2022-04-01",
                    "startDate": "2022-04-01"
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

      it("logs audit") {
        mockMvc.performAuthorised(path)

        verify(
          auditService,
          VerificationModeFactory.times(1),
        ).createEvent(
          "GET_PERSON_SENTENCES_LATEST_KEY_DATES_AND_ADJUSTMENTS",
          mapOf("hmppsId" to hmppsId),
        )
      }

      it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
        whenever(getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId, null)).thenReturn(
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

      it("returns a 500 INTERNAL SERVER ERROR status code when upstream api return expected error") {

        whenever(getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId, null)).doThrow(
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
