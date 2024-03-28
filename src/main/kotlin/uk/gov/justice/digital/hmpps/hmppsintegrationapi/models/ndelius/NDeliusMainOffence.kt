package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SystemSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.time.LocalDate

data class NDeliusMainOffence(
  val description: String? = null,
  val code: String? = null,
  val date: String? = null,
) {
  fun toOffence(courtDates: List<LocalDate>): Offence =
    Offence(
      serviceSource = UpstreamApi.NDELIUS,
      systemSource = SystemSource.PROBATION_SYSTEMS,
      cjsCode = null,
      hoCode = this.code,
      courtDates = courtDates,
      endDate = null,
      startDate = LocalDate.parse(this.date),
      statuteCode = null,
      description = this.description,
    )
}
