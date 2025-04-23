package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag

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

    val featureFlagValue = featureFlagConfig.fromDashSeparatedName(featureFlagName) ?: throw FeatureNotEnabledException("Feature flag not found: $featureFlagName")

    if (!featureFlagValue) {
      throw FeatureNotEnabledException("Feature flag is disabled: $featureFlagName")
    }

    return joinPoint.proceed()
  }
}
