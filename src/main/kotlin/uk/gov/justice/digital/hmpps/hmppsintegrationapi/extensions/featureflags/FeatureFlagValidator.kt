package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflags

interface FeatureFlagValidator {
  val featureFlagName: String

  fun validate(vararg args: Any?): Boolean
}
