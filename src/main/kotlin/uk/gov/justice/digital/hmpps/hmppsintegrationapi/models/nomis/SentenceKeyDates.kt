package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceKeyDates
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.HomeDetentionCurfewDate as HmppsHomeDetentionCurfewDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceKeyDate as HmppsSentenceKeyDate
data class SentenceKeyDates(
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
) {
  fun toSentenceKeyDates(): SentenceKeyDates = SentenceKeyDates(
    automaticRelease = HmppsSentenceKeyDate(date = this.automaticReleaseDate, overrideDate = this.automaticReleaseOverrideDate),
    conditionalRelease = HmppsSentenceKeyDate(date = this.conditionalReleaseDate, overrideDate = this.conditionalReleaseOverrideDate),
    dtoPostRecallRelease = HmppsSentenceKeyDate(date = this.dtoPostRecallReleaseDate, overrideDate = this.dtoPostRecallReleaseDateOverride),
    earlyTerm = HmppsSentenceKeyDate(date = this.earlyTermDate, overrideDate = this.etdOverrideDate, calculatedDate = this.etdCalculatedDate),
    homeDetentionCurfew = HmppsHomeDetentionCurfewDate(
      actualDate = this.homeDetentionCurfewActualDate,
      eligibilityCalculatedDate = this.homeDetentionCurfewEligibilityCalculatedDate,
      eligibilityDate = this.homeDetentionCurfewEligibilityDate,
      eligibilityOverrideDate = this.homeDetentionCurfewEligibilityOverrideDate,
      endDate = this.homeDetentionCurfewEndDate,
    ),
    lateTerm = HmppsSentenceKeyDate(date = this.lateTermDate, overrideDate = this.ltdOverrideDate, calculatedDate = this.ltdCalculatedDate),
    licenceExpiry = HmppsSentenceKeyDate(date = this.licenceExpiryDate, overrideDate = this.licenceExpiryOverrideDate, calculatedDate = this.licenceExpiryCalculatedDate),
    midTerm = HmppsSentenceKeyDate(date = this.midTermDate, overrideDate = this.mtdOverrideDate, calculatedDate = this.mtdCalculatedDate),
  )
}
