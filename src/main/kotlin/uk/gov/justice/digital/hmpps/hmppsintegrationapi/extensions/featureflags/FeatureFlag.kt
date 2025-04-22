package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflags

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class FeatureFlag(
  val validators: Array<KClass<out FeatureFlagValidator>>,
)
