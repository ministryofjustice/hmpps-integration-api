package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.Sentence as NDeliusSentence

data class Supervision(
  val mainOffence: MainOffence = MainOffence(),
  val additionalOffences: List<AdditionalOffence> = listOf(AdditionalOffence()),
  val courtAppearances: List<CourtAppearance> = listOf(CourtAppearance()),
  val sentence: NDeliusSentence = NDeliusSentence(),
  val active: Boolean? = null,
) {
  fun toOffences(): List<Offence> {
    val courtDates = this.courtAppearances.mapNotNull { LocalDate.parse(it.date, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
    return listOf(this.mainOffence.toOffence(courtDates)) + this.additionalOffences.map { it.toOffence(courtDates) }
  }

  fun toSentence(): Sentence {
    return Sentence(
      dateOfSentencing = LocalDate.parse(this.sentence.date),
      isActive = this.active,
    )
  }
}
