package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.AccessFor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.LaoContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.LaoRedaction.Mode
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse

@Configuration
@EnableAspectJAutoProxy
class AopConfig

@Aspect
@Component
class LaoRedactorAspect(
  private val loaChecker: AccessFor,
) {
  @Around("@annotation(redaction)")
  fun redact(
    joinPoint: ProceedingJoinPoint,
    redaction: LaoRedaction,
  ): Any {
    val hmppsId = (joinPoint.args.first() as String).decodeUrlCharacters()
    val laoContext = loaChecker.getAccessFor(hmppsId)?.asLaoContext()
    if (laoContext?.isLimitedAccess() == true && redaction.mode == Mode.REJECT) {
      throw LimitedAccessException()
    }
    val result = joinPoint.proceed()
    return if (laoContext?.isLimitedAccess() == true) {
      when (result) {
        is DataResponse<*> -> redactDataResponse(result)
        is PaginatedResponse<*> -> redactPaginatedResponse(result)
        else -> LaoRedactor.of(result)?.redact(result) ?: result
      }
    } else {
      result
    }
  }

  private fun redactDataResponse(dataResponse: DataResponse<*>): DataResponse<*> =
    dataResponse.data
      ?.let { LaoRedactor.of(it) }
      ?.redact(dataResponse.data)
      ?.let { DataResponse(it) } ?: dataResponse

  private fun redactPaginatedResponse(paginatedResponse: PaginatedResponse<*>): PaginatedResponse<*> {
    val redactor = paginatedResponse.data.firstOrNull()?.let { LaoRedactor.of(it) }
    return PaginatedResponse(
      paginatedResponse.data.map { redactor?.redact(it as Any) ?: it },
      paginatedResponse.pagination,
    )
  }

  private fun CaseAccess.asLaoContext() = LaoContext(crn, userExcluded, userRestricted, exclusionMessage, restrictionMessage)
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LaoRedaction(
  val mode: Mode = Mode.REDACT,
) {
  enum class Mode {
    REDACT,
    REJECT,
  }
}
