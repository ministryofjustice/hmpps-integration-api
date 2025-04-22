package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflags

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException

@Aspect
@Component
class FeatureFlagAspect(
  @Autowired
  private val applicationContext: ApplicationContext? = null,
) {
  @Around(value = "@annotation(featureFlag)", argNames = "featureFlag")
  @Throws(FeatureNotEnabledException::class)
  fun checkFeatureFlag(
    joinPoint: ProceedingJoinPoint,
    featureFlag: FeatureFlag,
  ): Any {
    val args = joinPoint.args
    for (validatorClass in featureFlag.validators) {
      val validator: FeatureFlagValidator =
        applicationContext?.getBean(validatorClass.java)
          ?: throw IllegalStateException("ApplicationContext is not initialized")
      if (!validator.validate(*args)) {
        throw FeatureNotEnabledException(validator.featureFlagName)
      }
    }
    return joinPoint.proceed()
  }
}
