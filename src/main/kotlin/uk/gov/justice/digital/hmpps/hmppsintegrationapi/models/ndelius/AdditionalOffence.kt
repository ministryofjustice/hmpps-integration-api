package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AdditionalOffence(
  val description: String? = null,
  val code: String? = null,
) {

  fun toOffence(courtAppearances: List<CourtAppearance>): Offence = Offence(
    cjsCode = null,
    hoCode = this.code,
    courtDates = courtAppearances.map { LocalDate.parse(it.date, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }.filterNotNull(),
    endDate = null,
    startDate = null,
    statuteCode = null,
    description = this.description,
  )
}
