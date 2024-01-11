package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
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
    startDate = if (!this.date.isNullOrEmpty()) LocalDate.parse(this.date) else null,
    statuteCode = null,
    description = this.description,
  )
}
