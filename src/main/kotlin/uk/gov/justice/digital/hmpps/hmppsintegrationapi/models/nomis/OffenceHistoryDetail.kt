package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Conviction
import java.time.LocalDate

data class OffenceHistoryDetail(
  val offenceDate: LocalDate,
  val offenceCode: String,
  val offenceDescription: String,
) {
  fun toConviction(): Conviction = Conviction(
    date = offenceDate,
    code = offenceCode,
    description = offenceDescription,
  )
}
