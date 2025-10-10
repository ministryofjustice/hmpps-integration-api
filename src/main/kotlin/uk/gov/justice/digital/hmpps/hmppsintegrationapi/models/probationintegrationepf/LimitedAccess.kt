package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationintegrationepf

data class LimitedAccess(
  val excludedFrom: List<AccessLimitation>,
  val exclusionMessage: String? = null,
  val restrictedTo: List<AccessLimitation>,
  val restrictionMessage: String? = null,
) {
  data class AccessLimitation(
    val email: String,
  )
}
