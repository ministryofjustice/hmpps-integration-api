package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig

data class Role(
  val name: String,
  val endpoints: List<String>,
)
