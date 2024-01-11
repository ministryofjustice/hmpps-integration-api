package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.time.LocalDate

data class LatestSentenceKeyDatesAndAdjustments(
  val adjustments: SentenceAdjustment? = null,
  val automaticRelease: SentenceKeyDate? = null,
  val conditionalRelease: SentenceKeyDate? = null,
  val dtoPostRecallRelease: SentenceKeyDate? = null,
  val earlyTerm: SentenceKeyDate? = null,
  val homeDetentionCurfew: HomeDetentionCurfewDate? = null,
  val lateTerm: SentenceKeyDate? = null,
  val licenceExpiry: SentenceKeyDate? = null,
  val midTerm: SentenceKeyDate? = null,
  val nonDto: NonDtoDate? = null,
  val nonParole: SentenceKeyDate? = null,
  val paroleEligibility: SentenceKeyDate? = null,
  val postRecallRelease: SentenceKeyDate? = null,
  val release: ReleaseDate? = null,
  val sentence: SentenceDate? = null,
  val topupSupervision: TopupSupervision? = null,
  val actualParoleDate: LocalDate? = null,
  val earlyRemovalSchemeEligibilityDate: LocalDate? = null,
  val releaseOnTemporaryLicenceDate: LocalDate? = null,
  val tariffDate: LocalDate? = null,
  val tariffEarlyRemovalSchemeEligibilityDate: LocalDate? = null,
)
data class KeyDatesAndAdjustmentsDTO(
  val adjustments: SentenceAdjustment? = null,
  val keyDates: SentenceKeyDates? = null,
) {
  fun toLatestSentenceKeyDatesAndAdjustments(): LatestSentenceKeyDatesAndAdjustments {
    return LatestSentenceKeyDatesAndAdjustments(
      adjustments = this.adjustments,
      automaticRelease = this.keyDates?.automaticRelease,
      conditionalRelease = this.keyDates?.conditionalRelease,
      dtoPostRecallRelease = this.keyDates?.dtoPostRecallRelease,
      earlyTerm = this.keyDates?.earlyTerm,
      homeDetentionCurfew = this.keyDates?.homeDetentionCurfew,
      lateTerm = this.keyDates?.lateTerm,
      licenceExpiry = this.keyDates?.licenceExpiry,
      midTerm = this.keyDates?.midTerm,
      nonDto = this.keyDates?.nonDto,
      nonParole = this.keyDates?.nonParole,
      paroleEligibility = this.keyDates?.paroleEligibility,
      postRecallRelease = this.keyDates?.postRecallRelease,
      release = this.keyDates?.release,
      sentence = this.keyDates?.sentence,
      topupSupervision = this.keyDates?.topupSupervision,
      actualParoleDate = this.keyDates?.actualParoleDate,
      earlyRemovalSchemeEligibilityDate = this.keyDates?.earlyRemovalSchemeEligibilityDate,
      releaseOnTemporaryLicenceDate = this.keyDates?.releaseOnTemporaryLicenceDate,
      tariffDate = this.keyDates?.tariffDate,
      tariffEarlyRemovalSchemeEligibilityDate = this.keyDates?.tariffEarlyRemovalSchemeEligibilityDate,
    )
  }
}
