package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class ActivityCategory(
  val id: Long,
  val code: String,
  val name: String,
  val description: String?,
)
