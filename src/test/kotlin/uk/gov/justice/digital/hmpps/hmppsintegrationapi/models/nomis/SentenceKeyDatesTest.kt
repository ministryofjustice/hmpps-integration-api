package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.SentenceKeyDates as SentenceKeyDatesFromNomis
class SentenceKeyDatesTest : DescribeSpec(
  {
    describe("#toSentenceKeyDates") {
      it("maps one-to-one attributes to integration API attributes") {
        val sentenceKeyDatesFromNomis = SentenceKeyDatesFromNomis(
          automaticReleaseDate = LocalDate.parse("2022-03-01"),
          automaticReleaseOverrideDate = LocalDate.parse("2022-03-01"),
          conditionalReleaseDate = LocalDate.parse("2022-04-01"),
          conditionalReleaseOverrideDate = LocalDate.parse("2022-04-01"),
          dtoPostRecallReleaseDate = LocalDate.parse("2022-05-01"),
          dtoPostRecallReleaseDateOverride = LocalDate.parse("2022-05-01"),
          earlyTermDate = LocalDate.parse("2021-04-01"),
          etdOverrideDate = LocalDate.parse("2021-04-01"),
          etdCalculatedDate = LocalDate.parse("2021-04-01"),
          homeDetentionCurfewActualDate = LocalDate.parse("2022-04-01"),
          homeDetentionCurfewEligibilityDate = LocalDate.parse("2022-04-01"),
          homeDetentionCurfewEligibilityCalculatedDate = LocalDate.parse("2022-04-01"),
          homeDetentionCurfewEligibilityOverrideDate = LocalDate.parse("2022-04-01"),
          homeDetentionCurfewEndDate = LocalDate.parse("2022-04-01"),
          lateTermDate = LocalDate.parse("2023-04-01"),
          ltdOverrideDate = LocalDate.parse("2023-04-01"),
          ltdCalculatedDate = LocalDate.parse("2023-04-01"),
          licenceExpiryDate = LocalDate.parse("2025-04-01"),
          licenceExpiryCalculatedDate = LocalDate.parse("2025-04-01"),
          licenceExpiryOverrideDate = LocalDate.parse("2025-04-01"),
          midTermDate = LocalDate.parse("2024-04-01"),
          mtdCalculatedDate = LocalDate.parse("2024-04-01"),
          mtdOverrideDate = LocalDate.parse("2024-04-01"),
          nonDtoReleaseDate = LocalDate.parse("2024-04-01"),
          nonDtoReleaseDateType = "CRD",
          nonParoleDate = LocalDate.parse("2026-04-01"),
          nonParoleOverrideDate = LocalDate.parse("2026-04-01"),
          paroleEligibilityDate = LocalDate.parse("2027-04-01"),
          paroleEligibilityCalculatedDate = LocalDate.parse("2027-04-01"),
          paroleEligibilityOverrideDate = LocalDate.parse("2027-04-01"),
          postRecallReleaseDate = LocalDate.parse("2028-04-01"),
          postRecallReleaseOverrideDate = LocalDate.parse("2028-04-01"),
        )

        val sentenceKeyDates = sentenceKeyDatesFromNomis.toSentenceKeyDates()

        sentenceKeyDates.automaticRelease.date.shouldBe(sentenceKeyDatesFromNomis.automaticReleaseDate)
        sentenceKeyDates.automaticRelease.overrideDate.shouldBe(sentenceKeyDatesFromNomis.automaticReleaseOverrideDate)

        sentenceKeyDates.conditionalRelease.date.shouldBe(sentenceKeyDatesFromNomis.conditionalReleaseDate)
        sentenceKeyDates.conditionalRelease.overrideDate.shouldBe(sentenceKeyDatesFromNomis.conditionalReleaseOverrideDate)

        sentenceKeyDates.dtoPostRecallRelease.date.shouldBe(sentenceKeyDatesFromNomis.dtoPostRecallReleaseDate)
        sentenceKeyDates.dtoPostRecallRelease.overrideDate.shouldBe(sentenceKeyDatesFromNomis.dtoPostRecallReleaseDateOverride)

        sentenceKeyDates.earlyTerm.date.shouldBe(sentenceKeyDatesFromNomis.earlyTermDate)
        sentenceKeyDates.earlyTerm.overrideDate.shouldBe(sentenceKeyDatesFromNomis.etdOverrideDate)
        sentenceKeyDates.earlyTerm.calculatedDate.shouldBe(sentenceKeyDatesFromNomis.etdCalculatedDate)

        sentenceKeyDates.homeDetentionCurfew.actualDate.shouldBe(sentenceKeyDatesFromNomis.homeDetentionCurfewActualDate)
        sentenceKeyDates.homeDetentionCurfew.eligibilityCalculatedDate.shouldBe(sentenceKeyDatesFromNomis.homeDetentionCurfewEligibilityCalculatedDate)
        sentenceKeyDates.homeDetentionCurfew.eligibilityDate.shouldBe(sentenceKeyDatesFromNomis.homeDetentionCurfewEligibilityDate)
        sentenceKeyDates.homeDetentionCurfew.eligibilityOverrideDate.shouldBe(sentenceKeyDatesFromNomis.homeDetentionCurfewEligibilityOverrideDate)
        sentenceKeyDates.homeDetentionCurfew.endDate.shouldBe(sentenceKeyDatesFromNomis.homeDetentionCurfewEndDate)

        sentenceKeyDates.lateTerm.date.shouldBe(sentenceKeyDatesFromNomis.lateTermDate)
        sentenceKeyDates.lateTerm.overrideDate.shouldBe(sentenceKeyDatesFromNomis.ltdOverrideDate)
        sentenceKeyDates.lateTerm.calculatedDate.shouldBe(sentenceKeyDatesFromNomis.ltdCalculatedDate)

        sentenceKeyDates.licenceExpiry.date.shouldBe(sentenceKeyDatesFromNomis.licenceExpiryDate)
        sentenceKeyDates.licenceExpiry.overrideDate.shouldBe(sentenceKeyDatesFromNomis.licenceExpiryOverrideDate)
        sentenceKeyDates.licenceExpiry.calculatedDate.shouldBe(sentenceKeyDatesFromNomis.licenceExpiryCalculatedDate)

        sentenceKeyDates.midTerm.date.shouldBe(sentenceKeyDatesFromNomis.midTermDate)
        sentenceKeyDates.midTerm.overrideDate.shouldBe(sentenceKeyDatesFromNomis.mtdOverrideDate)
        sentenceKeyDates.midTerm.calculatedDate.shouldBe(sentenceKeyDatesFromNomis.mtdCalculatedDate)

        sentenceKeyDates.nonDto.date.shouldBe(sentenceKeyDatesFromNomis.nonDtoReleaseDate)
        sentenceKeyDates.nonDto.releaseDateType.shouldBe(sentenceKeyDatesFromNomis.nonDtoReleaseDateType)

        sentenceKeyDates.nonParole.date.shouldBe(sentenceKeyDatesFromNomis.nonParoleDate)
        sentenceKeyDates.nonParole.overrideDate.shouldBe(sentenceKeyDatesFromNomis.nonParoleOverrideDate)

        sentenceKeyDates.paroleEligibility.date.shouldBe(sentenceKeyDatesFromNomis.paroleEligibilityDate)
        sentenceKeyDates.paroleEligibility.overrideDate.shouldBe(sentenceKeyDatesFromNomis.paroleEligibilityOverrideDate)
        sentenceKeyDates.paroleEligibility.calculatedDate.shouldBe(sentenceKeyDatesFromNomis.paroleEligibilityCalculatedDate)

        sentenceKeyDates.postRecallRelease.date.shouldBe(sentenceKeyDatesFromNomis.postRecallReleaseDate)
        sentenceKeyDates.postRecallRelease.overrideDate.shouldBe(sentenceKeyDatesFromNomis.postRecallReleaseOverrideDate)
      }
    }
  },
)
