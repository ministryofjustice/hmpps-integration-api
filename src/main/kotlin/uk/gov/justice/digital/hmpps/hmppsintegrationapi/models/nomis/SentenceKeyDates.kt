package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceKeyDates
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceKeyDate as HmppsSentenceKeyDate

data class SentenceKeyDates(
  val automaticReleaseDate: LocalDate? = null,
  val automaticReleaseOverrideDate: LocalDate? = null,
  val conditionalReleaseDate: LocalDate? = null,
  val conditionalReleaseOverrideDate: LocalDate? = null,
  val dtoPostRecallReleaseDate: LocalDate? = null,
  val dtoPostRecallReleaseDateOverride: LocalDate? = null,
) {
  fun toSentenceKeyDates(): SentenceKeyDates = SentenceKeyDates(
    automaticRelease = HmppsSentenceKeyDate(date = this.automaticReleaseDate, overrideDate = this.automaticReleaseOverrideDate),
    conditionalRelease = HmppsSentenceKeyDate(date = this.conditionalReleaseDate, overrideDate = this.conditionalReleaseOverrideDate),
    dtoPostRecallRelease = HmppsSentenceKeyDate(date = this.dtoPostRecallReleaseDate, overrideDate = this.dtoPostRecallReleaseDateOverride)
  )
}
