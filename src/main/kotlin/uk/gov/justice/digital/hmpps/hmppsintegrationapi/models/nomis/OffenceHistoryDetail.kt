package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
import java.time.LocalDate

data class OffenceHistoryDetail(
  val courtDate: LocalDate? = null,
  val offenceCode: String,
  val offenceDate: LocalDate? = null,
  val offenceDescription: String,
  val offenceRangeDate: LocalDate? = null,
  val statuteCode: String,
) {
  fun toOffence(): Offence = Offence(
    cjsCode = this.offenceCode,
    courtDates = listOf(this.courtDate).filterNotNull(),
    description = this.offenceDescription,
    endDate = this.offenceRangeDate,
    startDate = this.offenceDate,
    statuteCode = this.statuteCode,
  )
}
