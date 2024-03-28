package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SystemSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.time.LocalDate

data class NomisOffenceHistoryDetail(
  val courtDate: LocalDate? = null,
  val offenceCode: String,
  val offenceDate: LocalDate? = null,
  val offenceDescription: String,
  val offenceRangeDate: LocalDate? = null,
  val statuteCode: String,
) {
  fun toOffence(): Offence =
    Offence(
      serviceSource = UpstreamApi.NOMIS,
      systemSource = SystemSource.PRISON_SYSTEMS,
      cjsCode = this.offenceCode,
      courtDates = listOf(this.courtDate).filterNotNull(),
      description = this.offenceDescription,
      endDate = this.offenceRangeDate,
      startDate = this.offenceDate,
      statuteCode = this.statuteCode,
    )
}
