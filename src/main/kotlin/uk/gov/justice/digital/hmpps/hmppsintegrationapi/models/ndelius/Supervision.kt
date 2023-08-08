package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Supervision(
  val mainOffence: MainOffence = MainOffence(),
  val additionalOffences: List<AdditionalOffence> = listOf(AdditionalOffence()),
  val courtAppearances: List<CourtAppearance> = listOf(CourtAppearance()),
) {
  fun toOffences(): List<Offence> {
    val courtDates = courtAppearances.mapNotNull { LocalDate.parse(it.date, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
    return listOf(this.mainOffence.toOffence(courtDates)) + this.additionalOffences.map { it.toOffence(courtDates) }
  }
}
