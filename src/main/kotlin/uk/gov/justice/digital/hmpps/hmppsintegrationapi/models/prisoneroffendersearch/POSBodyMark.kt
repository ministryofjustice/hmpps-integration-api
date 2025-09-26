package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.BodyMark

data class POSBodyMark(
  val bodyPart: String?,
  val comment: String?,
) {
  fun toBodyMark() = BodyMark(bodyPart = this.bodyPart, comment = this.comment)
}
