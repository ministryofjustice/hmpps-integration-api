package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.oboconfig

data class OboConfig(
  val strategy: String,
  val required: Boolean = true,
)
