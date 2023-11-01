package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceKeyDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceKeyDates
import java.time.LocalDate

data class SentenceKeyDates(
  val automaticReleaseDate: LocalDate? = null,
) {
  fun toSentenceKeyDates(): SentenceKeyDates = SentenceKeyDates(
    automaticRelease = SentenceKeyDate(date = automaticReleaseDate),
  )
}
