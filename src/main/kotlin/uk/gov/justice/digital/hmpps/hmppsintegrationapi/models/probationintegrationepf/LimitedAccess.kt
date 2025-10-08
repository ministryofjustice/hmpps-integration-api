package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationintegrationepf

data class LimitedAccess(
  val excludedFrom: List<AccessLimitation>,
  val restrictedTo: List<AccessLimitation>,
) {
  data class AccessLimitation(
    val email: String,
    val message: String? = null,
  )
}
