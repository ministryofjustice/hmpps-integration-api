package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.AccessFor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.LaoContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.LaoRedaction.Mode
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess

@Configuration
@EnableAspectJAutoProxy
class AopConfig

@Aspect
@Component
class LaoRedactorAspect(
  private val loaChecker: AccessFor,
  private val featureFlagConfig: FeatureFlagConfig,
) {
  @Around("@annotation(redaction)")
  fun redact(
    joinPoint: ProceedingJoinPoint,
    redaction: LaoRedaction,
  ): Any {
    val hmppsId = (joinPoint.args.first() as String).decodeUrlCharacters()
    val laoContext = loaChecker.getAccessFor(hmppsId)?.asLaoContext() ?: throw LimitedAccessFailedException()

    if (laoContext.isLimitedAccess() && redaction.mode == Mode.REJECT) {
      throw LimitedAccessException()
    }
    return joinPoint.proceed()
  }

  private fun CaseAccess.asLaoContext() = LaoContext(crn, userExcluded, userRestricted, exclusionMessage, restrictionMessage)
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LaoRedaction(
  val mode: Mode,
) {
  enum class Mode {
    REJECT,
  }
}
