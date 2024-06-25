package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.StatusInformation

class NDeliusPersonStatus(
  val code: String? = null,
  val description: String? = null,
  val startDate: String? = null,
  val reviewDate: String? = null,
  val notes: String? = null,
) {
  fun toStatusInformation(): StatusInformation =
    StatusInformation(
      code = this.code,
      description = this.description,
      startDate = this.startDate,
      reviewDate = this.reviewDate,
      notes = this.notes,
    )
}
