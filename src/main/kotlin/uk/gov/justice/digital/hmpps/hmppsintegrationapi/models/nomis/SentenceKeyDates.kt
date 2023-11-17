package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceKeyDate as HmppsSentenceKeyDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.SentenceKeyDate as NomisSentenceKeyDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceKeyDates

data class SentenceKeyDates(
  val automaticReleaseDate: NomisSentenceKeyDate = NomisSentenceKeyDate(),
) {
  fun toSentenceKeyDates(): SentenceKeyDates = SentenceKeyDates(
    automaticRelease = HmppsSentenceKeyDate(date = automaticReleaseDate.date),
  )
}
