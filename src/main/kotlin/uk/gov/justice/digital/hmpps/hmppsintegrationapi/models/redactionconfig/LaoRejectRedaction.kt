package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.RedactionContext

class LaoRejectRedaction(
  val paths: List<String>? = null,
) : ResponseRedaction {
  private val pathPatterns: List<Regex>? = paths?.map(::Regex)

  override fun apply(
    policyName: String,
    redactionContext: RedactionContext,
    responseBody: Any,
  ): Any {
    var shouldRun = pathPatterns?.any { it.matches(redactionContext.requestUri) } ?: true
    if (!shouldRun) return responseBody
    if (redactionContext.isLimitedAccessOffender()) {
      redactionContext.trackRedaction(policyName, rejects = 1)
      throw LimitedAccessException()
    }
    return responseBody
  }
}
