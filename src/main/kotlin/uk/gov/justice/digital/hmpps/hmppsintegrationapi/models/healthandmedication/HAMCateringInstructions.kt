package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CateringInstruction

data class HAMCateringInstructions(
  val value: String?,
  val lastModifiedAt: String,
  val lastModifiedBy: String,
  val lastModifiedPrisonId: String,
) {
  fun toCateringInstruction() =
    CateringInstruction(
      value = this.value,
    )
}
