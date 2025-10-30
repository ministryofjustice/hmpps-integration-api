package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService

class RedactionContext(
  val requestUri: String,
  val hasAccess: GetCaseAccess,
  val telemetryService: TelemetryService,
  val hmppsId: String? = null,
) {
  fun isLimitedAccessOffender(): Boolean {
    val hmppsId = hmppsId ?: throw LimitedAccessFailedException("No hmppsId available for LAO check")
    return hasAccess.getAccessFor(hmppsId)?.let { it.userRestricted || it.userExcluded } ?: throw LimitedAccessFailedException()
  }

  fun trackRedaction(type: RedactionType) = telemetryService.trackEvent("RedactionEvent", mapOf("type" to type.name, "uri" to requestUri))
}
