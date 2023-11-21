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
  )
}
