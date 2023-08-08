package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import java.time.LocalDate

data class AdditionalOffence(
  val description: String? = null,
  val code: String? = null,
  val date: String? = null,
) {
  fun toOffence(courtDates: List<LocalDate>): Offence = Offence(
    cjsCode = null,
    hoCode = this.code,
    courtDates = courtDates,
    endDate = null,
    startDate = LocalDate.parse(this.date),
    statuteCode = null,
    description = this.description,
  )
}
