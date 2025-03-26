package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess

data class LaoContext(
  val crn: String,
  val excluded: Boolean,
  val restricted: Boolean,
  val excludedMessage: String?,
  val restrictedMessage: String?,
) {
  fun isLimitedAccess() = excluded || restricted
}
