package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence

data class Supervision(
  val mainOffence: MainOffence = MainOffence(),
) {
  fun toOffence(): Offence = Offence(
    cjsCode = null,
    courtDate = null,
    endDate = null,
    startDate = null,
    statuteCode = null,
    description = this.mainOffence?.description,
  )
}
