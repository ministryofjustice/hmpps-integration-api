package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.oboconfig

data class OboConfig(
  val strategy: String,
  val required: Boolean = true,
  val verificationStrategy: OboVerificationStrategy? = null,
)

data class OboVerificationStrategy(
  val validate: Boolean = false,
  val authSources: List<String> = emptyList(),
)
