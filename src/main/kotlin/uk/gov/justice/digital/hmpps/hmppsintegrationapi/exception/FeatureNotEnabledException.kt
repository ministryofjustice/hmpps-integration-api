package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

data class FeatureNotEnabledException(
  val feature: String,
) : RuntimeException("$feature not enabled")
