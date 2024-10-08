package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SystemSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.time.LocalDate

data class NDeliusAdditionalOffence(
  val description: String? = null,
  val code: String? = null,
  val date: String? = null,
) {
  fun toOffence(
    courtDates: List<LocalDate>,
    courtName: String?,
  ): Offence =
    Offence(
      serviceSource = UpstreamApi.NDELIUS,
      systemSource = SystemSource.PROBATION_SYSTEMS,
      cjsCode = null,
      hoCode = this.code,
      courtDates = courtDates,
      courtName = courtName,
      endDate = null,
      startDate = if (!this.date.isNullOrEmpty()) LocalDate.parse(this.date) else null,
      statuteCode = null,
      description = this.description,
    )
}
