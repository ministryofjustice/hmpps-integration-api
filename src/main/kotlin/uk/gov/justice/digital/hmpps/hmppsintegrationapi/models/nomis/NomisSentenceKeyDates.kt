package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HomeDetentionCurfewDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NonDtoDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReleaseDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceKeyDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceKeyDates
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TopupSupervision
import java.time.LocalDate

data class NomisSentenceKeyDates(
  val automaticReleaseDate: LocalDate? = null,
  val automaticReleaseOverrideDate: LocalDate? = null,
  val conditionalReleaseDate: LocalDate? = null,
  val conditionalReleaseOverrideDate: LocalDate? = null,
  val dtoPostRecallReleaseDate: LocalDate? = null,
  val dtoPostRecallReleaseDateOverride: LocalDate? = null,
  val earlyTermDate: LocalDate? = null,
  val etdOverrideDate: LocalDate? = null,
  val etdCalculatedDate: LocalDate? = null,
  val homeDetentionCurfewActualDate: LocalDate? = null,
  val homeDetentionCurfewEligibilityDate: LocalDate? = null,
  val homeDetentionCurfewEligibilityCalculatedDate: LocalDate? = null,
  val homeDetentionCurfewEligibilityOverrideDate: LocalDate? = null,
  val homeDetentionCurfewEndDate: LocalDate? = null,
  val lateTermDate: LocalDate? = null,
  val ltdOverrideDate: LocalDate? = null,
  val ltdCalculatedDate: LocalDate? = null,
  val licenceExpiryDate: LocalDate? = null,
  val licenceExpiryCalculatedDate: LocalDate? = null,
  val licenceExpiryOverrideDate: LocalDate? = null,
  val midTermDate: LocalDate? = null,
  val mtdCalculatedDate: LocalDate? = null,
  val mtdOverrideDate: LocalDate? = null,
  val nonDtoReleaseDate: LocalDate? = null,
  val nonDtoReleaseDateType: String? = null,
  val nonParoleDate: LocalDate? = null,
  val nonParoleOverrideDate: LocalDate? = null,
  val paroleEligibilityDate: LocalDate? = null,
  val paroleEligibilityCalculatedDate: LocalDate? = null,
  val paroleEligibilityOverrideDate: LocalDate? = null,
  val postRecallReleaseDate: LocalDate? = null,
  val postRecallReleaseOverrideDate: LocalDate? = null,
  val releaseDate: LocalDate? = null,
  val confirmedReleaseDate: LocalDate? = null,
  val effectiveSentenceEndDate: LocalDate? = null,
  val sentenceExpiryCalculatedDate: LocalDate? = null,
  val sentenceExpiryDate: LocalDate? = null,
  val sentenceExpiryOverrideDate: LocalDate? = null,
  val sentenceStartDate: LocalDate? = null,
  val topupSupervisionExpiryCalculatedDate: LocalDate? = null,
  val topupSupervisionExpiryDate: LocalDate? = null,
  val topupSupervisionExpiryOverrideDate: LocalDate? = null,
  val topupSupervisionStartDate: LocalDate? = null,
  val actualParoleDate: LocalDate? = null,
  val earlyRemovalSchemeEligibilityDate: LocalDate? = null,
  val releaseOnTemporaryLicenceDate: LocalDate? = null,
  val tariffDate: LocalDate? = null,
  val tariffEarlyRemovalSchemeEligibilityDate: LocalDate? = null,
) {
  fun toSentenceKeyDates(): SentenceKeyDates =
    SentenceKeyDates(
      automaticRelease = SentenceKeyDate(date = this.automaticReleaseDate, overrideDate = this.automaticReleaseOverrideDate),
      conditionalRelease = SentenceKeyDate(date = this.conditionalReleaseDate, overrideDate = this.conditionalReleaseOverrideDate),
      dtoPostRecallRelease = SentenceKeyDate(date = this.dtoPostRecallReleaseDate, overrideDate = this.dtoPostRecallReleaseDateOverride),
      earlyTerm = SentenceKeyDate(date = this.earlyTermDate, overrideDate = this.etdOverrideDate, calculatedDate = this.etdCalculatedDate),
      homeDetentionCurfew =
        HomeDetentionCurfewDate(
          actualDate = this.homeDetentionCurfewActualDate,
          eligibilityCalculatedDate = this.homeDetentionCurfewEligibilityCalculatedDate,
          eligibilityDate = this.homeDetentionCurfewEligibilityDate,
          eligibilityOverrideDate = this.homeDetentionCurfewEligibilityOverrideDate,
          endDate = this.homeDetentionCurfewEndDate,
        ),
      lateTerm = SentenceKeyDate(date = this.lateTermDate, overrideDate = this.ltdOverrideDate, calculatedDate = this.ltdCalculatedDate),
      licenceExpiry =
        SentenceKeyDate(
          date = this.licenceExpiryDate,
          overrideDate = this.licenceExpiryOverrideDate,
          calculatedDate = this.licenceExpiryCalculatedDate,
        ),
      midTerm = SentenceKeyDate(date = this.midTermDate, overrideDate = this.mtdOverrideDate, calculatedDate = this.mtdCalculatedDate),
      nonDto = NonDtoDate(date = this.nonDtoReleaseDate, releaseDateType = this.nonDtoReleaseDateType),
      nonParole = SentenceKeyDate(date = this.nonParoleDate, overrideDate = this.nonParoleOverrideDate),
      paroleEligibility =
        SentenceKeyDate(
          date = this.paroleEligibilityDate,
          overrideDate = this.paroleEligibilityOverrideDate,
          calculatedDate = this.paroleEligibilityCalculatedDate,
        ),
      postRecallRelease = SentenceKeyDate(date = this.postRecallReleaseDate, overrideDate = this.postRecallReleaseOverrideDate),
      release = ReleaseDate(date = this.releaseDate, confirmedDate = this.confirmedReleaseDate),
      sentence =
        SentenceDate(
          effectiveEndDate = this.effectiveSentenceEndDate,
          expiryCalculatedDate = this.sentenceExpiryCalculatedDate,
          expiryDate = this.sentenceExpiryDate,
          expiryOverrideDate = this.sentenceExpiryOverrideDate,
          startDate = this.sentenceStartDate,
        ),
      topupSupervision =
        TopupSupervision(
          expiryCalculatedDate = this.topupSupervisionExpiryCalculatedDate,
          expiryDate = this.topupSupervisionExpiryDate,
          expiryOverrideDate = this.topupSupervisionExpiryOverrideDate,
          startDate = this.topupSupervisionStartDate,
        ),
      actualParoleDate = this.actualParoleDate,
      earlyRemovalSchemeEligibilityDate = this.earlyRemovalSchemeEligibilityDate,
      releaseOnTemporaryLicenceDate = this.releaseOnTemporaryLicenceDate,
      tariffDate = this.tariffDate,
      tariffEarlyRemovalSchemeEligibilityDate = this.tariffEarlyRemovalSchemeEligibilityDate,
    )
}
