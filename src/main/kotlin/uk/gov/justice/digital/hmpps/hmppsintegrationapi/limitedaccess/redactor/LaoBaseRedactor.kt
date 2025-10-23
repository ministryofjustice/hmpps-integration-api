package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.AccessFor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.LaoContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess

abstract class LaoBaseRedactor<T : Any>(
  protected val loaChecker: AccessFor,
) : Redactor<T> {
  protected fun getLaoContext(): LaoContext? =
    getHmppsIdFromRequest()
      ?.let { hmppsId ->
        loaChecker
          .getAccessFor(hmppsId)
          ?.asLaoContext()
          ?: throw LimitedAccessFailedException()
      }
      ?: throw LimitedAccessFailedException()

  private fun CaseAccess.asLaoContext() = LaoContext(crn, userExcluded, userRestricted, exclusionMessage, restrictionMessage)

  @Suppress("UNCHECKED_CAST")
  protected fun getHmppsIdFromRequest(): String? {
    val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
    return request?.getAttribute("hmppsId") as? String
  }

  fun currentRequest(): HttpServletRequest? = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request

  protected fun requireDataResponse(toRedact: Any): DataResponse<*> =
    toRedact as? DataResponse<*>
      ?: throw IllegalArgumentException("Expected DataResponse, got ${toRedact::class.simpleName}")

  protected fun fail(toRedact: Any): Nothing = throw IllegalArgumentException("${this::class.simpleName} cannot redact ${toRedact::class.simpleName}")
}
