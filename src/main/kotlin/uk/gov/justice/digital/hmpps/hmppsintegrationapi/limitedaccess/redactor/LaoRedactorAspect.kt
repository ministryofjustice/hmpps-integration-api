package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.LaoContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse

@Configuration
@EnableAspectJAutoProxy
class AopConfig

@Aspect
@Component
class LaoRedactorAspect {
  @Around("@annotation(org.springframework.web.bind.annotation.GetMapping)")
  fun redact(joinPoint: ProceedingJoinPoint): Any {
    val result = joinPoint.proceed()
    if (LaoContext.get()?.isLimitedAccess() != true) return result
    return when (result) {
      is DataResponse<*> -> redactDataResponse(result)
      is PaginatedResponse<*> -> redactPaginatedResponse(result)
      else -> LaoRedactor.of(result)?.redact(result) ?: result
    }
  }

  private fun redactDataResponse(dataResponse: DataResponse<*>): DataResponse<*> = LaoRedactor.of(dataResponse.data as Any)?.redact(dataResponse.data)?.let { DataResponse(it) } ?: dataResponse

  private fun redactPaginatedResponse(paginatedResponse: PaginatedResponse<*>): PaginatedResponse<*> {
    val redactor = paginatedResponse.data.firstOrNull()?.let { LaoRedactor.of(it) }
    return PaginatedResponse(
      paginatedResponse.data.map { redactor?.redact(it as Any) ?: it },
      paginatedResponse.pagination,
    )
  }
}
