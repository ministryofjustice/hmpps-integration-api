package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.oboconfig

data class OboUser(
  val username: String,
  val hasPrisonRole: Boolean? = null,
)
