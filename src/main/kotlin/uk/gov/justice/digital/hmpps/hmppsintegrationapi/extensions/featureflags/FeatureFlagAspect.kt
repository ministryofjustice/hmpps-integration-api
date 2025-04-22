package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflags

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException

@Aspect
@Component
class FeatureFlagAspect(
  @Autowired
  private val featureFlagConfig: FeatureFlagConfig,
) {
  @Around(value = "@annotation(featureFlag)", argNames = "featureFlag")
  @Throws(FeatureNotEnabledException::class)
  fun checkFeatureFlag(
    joinPoint: ProceedingJoinPoint,
    featureFlag: FeatureFlag,
  ): Any {
    val featureFlagName = featureFlag.name

    val nameOfField = featureFlagConfig.fromDashSeparatedName(featureFlagName) ?: throw FeatureNotEnabledException("Feature flag not found: $featureFlagName")

    val fieldValue =
      FeatureFlagConfig::class
        .members
        .firstOrNull { it.name == nameOfField }
        ?.call(featureFlagConfig) as? Boolean
        ?: throw FeatureNotEnabledException("Feature flag not found or not a Boolean: $nameOfField")

    if (!fieldValue) {
      throw FeatureNotEnabledException("Feature flag is disabled: $nameOfField")
    }

    return joinPoint.proceed()
  }
}
