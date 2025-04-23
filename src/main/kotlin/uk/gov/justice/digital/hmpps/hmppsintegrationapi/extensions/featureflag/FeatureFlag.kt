package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class FeatureFlag(
  val name: String,
)
