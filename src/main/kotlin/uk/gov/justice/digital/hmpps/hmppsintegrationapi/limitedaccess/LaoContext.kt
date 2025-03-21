package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess

import org.springframework.web.context.request.RequestContextHolder

data class LaoContext(
  val crn: String,
  val excluded: Boolean,
  val restricted: Boolean,
  val excludedMessage: String?,
  val restrictedMessage: String?,
) {
  fun isLimitedAccess() = excluded || restricted

  companion object {
    fun get(): LaoContext? = RequestContextHolder.getRequestAttributes()?.getAttribute(LaoContext::class.simpleName!!, 0) as LaoContext?
  }
}
