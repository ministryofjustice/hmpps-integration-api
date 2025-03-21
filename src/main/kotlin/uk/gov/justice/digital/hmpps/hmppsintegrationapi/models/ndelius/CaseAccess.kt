package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

data class CaseAccess(
  val crn: String,
  val userExcluded: Boolean,
  val userRestricted: Boolean,
  val exclusionMessage: String? = null,
  val restrictionMessage: String? = null,
)

data class UserAccess(
  val access: List<CaseAccess>,
)
