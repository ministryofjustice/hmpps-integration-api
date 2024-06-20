package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DynamicRisk

data class NDeliusDynamicRisk(
  val code: String? = null,
  val description: String? = null,
  val startDate: String? = null,
  val reviewDate: String? = null,
  val notes: String? = null,
) {
  fun toDynamicRisk(): DynamicRisk =
    DynamicRisk(
      code = this.code,
      description = this.description,
      startDate = this.startDate,
      reviewDate = this.reviewDate,
      notes = this.notes,
    )
}
