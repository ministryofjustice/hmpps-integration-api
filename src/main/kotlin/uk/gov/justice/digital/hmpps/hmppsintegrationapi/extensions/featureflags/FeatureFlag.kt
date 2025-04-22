package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflags

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class FeatureFlag(
  val name: String,
)
