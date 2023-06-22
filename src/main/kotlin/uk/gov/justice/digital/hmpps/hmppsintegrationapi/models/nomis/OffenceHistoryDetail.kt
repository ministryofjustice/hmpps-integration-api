package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import java.time.LocalDate

data class OffenceHistoryDetail(
  val offenceDate: LocalDate,
  val offenceCode: String,
  val offenceDescription: String,
) {
  fun toOffence(): Offence = Offence(
    date = offenceDate,
    code = offenceCode,
    description = offenceDescription,
  )
}
