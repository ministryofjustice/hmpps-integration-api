package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.HomeDetentionCurfewDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.NonDtoDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ReleaseDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceAdjustment
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceKeyDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceKeyDates
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetLatestSentenceKeyDatesAndAdjustmentsForPersonService::class],
)
internal class GetLatestSentenceKeyDatesAndAdjustmentsForPersonServiceTest(
  @MockBean val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @MockBean val nomisGateway: NomisGateway,
  private val getLatestSentenceKeyDatesAndAdjustmentsForPersonService: GetLatestSentenceKeyDatesAndAdjustmentsForPersonService,
) : DescribeSpec(
  {
    val hmppsId = "2003/13116M"
    val nomisNumber = "abc123"

    beforeEach {
      Mockito.reset(nomisGateway)

      whenever(probationOffenderSearchGateway.getPerson(id = hmppsId)).thenReturn(
        Response(
          data = Person(
            firstName = "Baylan",
            lastName = "Skoll",
            identifiers = Identifiers(nomisNumber = nomisNumber),
          ),
        ),
      )
      whenever(nomisGateway.getLatestSentenceKeyDatesForPerson(nomisNumber)).thenReturn(
        Response(
          data = SentenceKeyDates(
            automaticRelease = SentenceKeyDate(date = LocalDate.parse("2023-11-02"), overrideDate = LocalDate.parse("2023-11-02")),
            conditionalRelease = SentenceKeyDate(date = LocalDate.parse("2023-12-02"), overrideDate = LocalDate.parse("2023-12-02")),
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
            actualParoleDate = LocalDate.parse("2031-02-01"),
            earlyRemovalSchemeEligibilityDate = LocalDate.parse("2031-02-01"),
            releaseOnTemporaryLicenceDate = LocalDate.parse("2031-02-01"),
            tariffDate = LocalDate.parse("2031-02-01"),
            tariffEarlyRemovalSchemeEligibilityDate = LocalDate.parse("2031-02-01"),
          ),
        ),
      )
      whenever(nomisGateway.getLatestSentenceAdjustmentsForPerson(nomisNumber)).thenReturn(
        Response(
          data = SentenceAdjustment(
            additionalDaysAwarded = 10,
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
        ),
      )
    }

    it("retrieves NOMIS number from Probation Offender Search") {
      getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId)

      verify(probationOffenderSearchGateway, VerificationModeFactory.times(1)).getPerson(id = hmppsId)
    }

    it("retrieves latest sentence key dates from NOMIS") {
      getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId)

      verify(nomisGateway, VerificationModeFactory.times(1)).getLatestSentenceKeyDatesForPerson(id = nomisNumber)
    }

    it("retrieves latest sentence adjustments from NOMIS") {
      getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId)

      verify(nomisGateway, VerificationModeFactory.times(1)).getLatestSentenceAdjustmentsForPerson(id = nomisNumber)
    }

    it("returns latest sentence key dates and adjustments from NOMIS") {
      val response = getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId)

      response.data?.adjustments?.additionalDaysAwarded.shouldBe(10)
      response.data?.adjustments?.unlawfullyAtLarge.shouldBe(10)
      response.data?.adjustments?.lawfullyAtLarge.shouldBe(2)
      response.data?.adjustments?.restoredAdditionalDaysAwarded.shouldBe(0)
      response.data?.adjustments?.specialRemission.shouldBe(11)
      response.data?.adjustments?.recallSentenceRemand.shouldBe(1)
      response.data?.adjustments?.recallSentenceTaggedBail.shouldBe(3)
      response.data?.adjustments?.remand.shouldBe(6)
      response.data?.adjustments?.taggedBail.shouldBe(3)
      response.data?.adjustments?.unusedRemand.shouldBe(6)

      response.data?.automaticRelease?.date.shouldBe(LocalDate.parse("2023-11-02"))
      response.data?.automaticRelease?.overrideDate.shouldBe(LocalDate.parse("2023-11-02"))
      response.data?.automaticRelease?.calculatedDate.shouldBe(null)

      response.data?.conditionalRelease?.date.shouldBe(LocalDate.parse("2023-12-02"))
      response.data?.conditionalRelease?.overrideDate.shouldBe(LocalDate.parse("2023-12-02"))
      response.data?.conditionalRelease?.calculatedDate.shouldBe(null)

      response.data?.dtoPostRecallRelease?.date.shouldBe(LocalDate.parse("2024-01-02"))
      response.data?.dtoPostRecallRelease?.overrideDate.shouldBe(LocalDate.parse("2024-01-02"))
      response.data?.dtoPostRecallRelease?.calculatedDate.shouldBe(null)

      response.data?.earlyTerm?.date.shouldBe(LocalDate.parse("2021-11-02"))
      response.data?.earlyTerm?.overrideDate.shouldBe(LocalDate.parse("2021-11-02"))
      response.data?.earlyTerm?.calculatedDate.shouldBe(LocalDate.parse("2021-11-02"))

      response.data?.homeDetentionCurfew?.actualDate.shouldBe(LocalDate.parse("2022-04-01"))
      response.data?.homeDetentionCurfew?.eligibilityCalculatedDate.shouldBe(LocalDate.parse("2022-04-01"))
      response.data?.homeDetentionCurfew?.eligibilityDate.shouldBe(LocalDate.parse("2022-04-01"))
      response.data?.homeDetentionCurfew?.eligibilityOverrideDate.shouldBe(LocalDate.parse("2022-04-01"))
      response.data?.homeDetentionCurfew?.endDate.shouldBe(LocalDate.parse("2022-04-01"))

      response.data?.lateTerm?.date.shouldBe(LocalDate.parse("2022-01-01"))
      response.data?.lateTerm?.overrideDate.shouldBe(LocalDate.parse("2022-01-01"))
      response.data?.lateTerm?.calculatedDate.shouldBe(LocalDate.parse("2022-01-01"))

      response.data?.licenceExpiry?.date.shouldBe(LocalDate.parse("2025-02-01"))
      response.data?.licenceExpiry?.overrideDate.shouldBe(LocalDate.parse("2025-02-01"))
      response.data?.licenceExpiry?.calculatedDate.shouldBe(LocalDate.parse("2025-02-01"))

      response.data?.midTerm?.date.shouldBe(LocalDate.parse("2024-02-01"))
      response.data?.midTerm?.overrideDate.shouldBe(LocalDate.parse("2024-02-01"))
      response.data?.midTerm?.calculatedDate.shouldBe(LocalDate.parse("2024-02-01"))

      response.data?.nonDto?.date.shouldBe(LocalDate.parse("2024-02-01"))
      response.data?.nonDto?.releaseDateType.shouldBe("CRD")

      response.data?.nonParole?.date.shouldBe(LocalDate.parse("2026-11-02"))
      response.data?.nonParole?.overrideDate.shouldBe(LocalDate.parse("2026-11-02"))
      response.data?.nonParole?.calculatedDate.shouldBe(null)

      response.data?.paroleEligibility?.date.shouldBe(LocalDate.parse("2027-02-01"))
      response.data?.paroleEligibility?.overrideDate.shouldBe(LocalDate.parse("2027-02-01"))
      response.data?.paroleEligibility?.calculatedDate.shouldBe(LocalDate.parse("2027-02-01"))

      response.data?.postRecallRelease?.date.shouldBe(LocalDate.parse("2028-02-01"))
      response.data?.postRecallRelease?.overrideDate.shouldBe(LocalDate.parse("2028-02-01"))
      response.data?.postRecallRelease?.calculatedDate.shouldBe(null)

      response.data?.release?.date.shouldBe(LocalDate.parse("2030-02-01"))
      response.data?.release?.confirmedDate.shouldBe(LocalDate.parse("2030-02-01"))

      response.data?.actualParoleDate?.shouldBe(LocalDate.parse("2031-02-01"))
      response.data?.earlyRemovalSchemeEligibilityDate?.shouldBe(LocalDate.parse("2031-02-01"))
      response.data?.releaseOnTemporaryLicenceDate?.shouldBe(LocalDate.parse("2031-02-01"))
      response.data?.tariffDate?.shouldBe(LocalDate.parse("2031-02-01"))
      response.data?.tariffEarlyRemovalSchemeEligibilityDate?.shouldBe(LocalDate.parse("2031-02-01"))
    }

    it("returns an error when person cannot be found in probation") {
      whenever(probationOffenderSearchGateway.getPerson(id = hmppsId)).thenReturn(
        Response(
          data = null,
          errors = listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
        ),
      )

      val response = getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId)

      response.errors.shouldHaveSize(1)
      response.errors.first().causedBy.shouldBe(UpstreamApi.PROBATION_OFFENDER_SEARCH)
      response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
    }

    it("returns null when person doesn't have a NOMIS number") {
      whenever(probationOffenderSearchGateway.getPerson(id = hmppsId)).thenReturn(
        Response(
          data = Person(
            firstName = "Shin",
            lastName = "Hati",
            identifiers = Identifiers(nomisNumber = null),
          ),
          errors = emptyList(),
        ),
      )

      val response = getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId)

      response.data.shouldBeNull()
    }

    it("returns all errors when latest sentence key dates and adjustments cannot be found for NOMIS number") {
      whenever(nomisGateway.getLatestSentenceKeyDatesForPerson(nomisNumber)).thenReturn(
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
      whenever(nomisGateway.getLatestSentenceAdjustmentsForPerson(nomisNumber)).thenReturn(
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

      val response = getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId)

      response.errors.shouldHaveSize(2)
    }
  },
)
