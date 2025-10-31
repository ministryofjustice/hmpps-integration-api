package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService

class RedactionContext(
  val requestUri: String,
  val hasAccess: GetCaseAccess,
  val telemetryService: TelemetryService,
  val hmppsId: String? = null,
  val clientId: String? = null,
) {
  fun isLimitedAccessOffender(): Boolean {
    val hmppsId = hmppsId ?: throw LimitedAccessFailedException("No hmppsId available for LAO check")
    return hasAccess.getAccessFor(hmppsId)?.let { it.userRestricted || it.userExcluded } ?: throw LimitedAccessFailedException()
  }

  fun trackRedaction(
    policyName: String,
    masks: Int,
    removes: Int,
  ) {
    if (masks > 0 || removes > 0) {
      telemetryService.trackEvent(
        "RedactionEvent",
        mapOf(
          "policyName" to policyName,
          "clientId" to clientId,
          "masks" to masks.toString(),
          "removes" to removes.toString(),
        ),
      )
    }
  }
}
