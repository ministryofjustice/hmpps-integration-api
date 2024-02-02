package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class OutcomeDto(
  val code: String? = null,
  val details: String? = null,
  val reason: String? = null,
  val quashedReason: String? = null,
  val canRemove: Boolean? = null,
)
