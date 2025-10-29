package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess

class RedactionContext(
  val requestUri: String,
  val hasAccess: GetCaseAccess,
  val hmppsId: String? = null,
) {
  fun isLimitedAccessOffender(): Boolean {
    val hmppsId = hmppsId ?: throw LimitedAccessFailedException("No hmppsId available for LAO check")
    return hasAccess.getAccessFor(hmppsId)?.let { it.userRestricted || it.userExcluded } ?: throw LimitedAccessFailedException()
  }
}
