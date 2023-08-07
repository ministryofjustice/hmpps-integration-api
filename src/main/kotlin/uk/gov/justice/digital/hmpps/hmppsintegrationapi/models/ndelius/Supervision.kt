package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Supervision(
  val mainOffence: MainOffence = MainOffence(),
  val courtAppearances: List<CourtAppearance> = listOf(CourtAppearance()),
) {
  fun toOffence(): Offence = Offence(
    cjsCode = null,
    courtDates = this.courtAppearances.map { LocalDate.parse(it.date, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }.filterNotNull(),
    endDate = null,
    startDate = null,
    statuteCode = null,
    description = this.mainOffence?.description,
  )
}
