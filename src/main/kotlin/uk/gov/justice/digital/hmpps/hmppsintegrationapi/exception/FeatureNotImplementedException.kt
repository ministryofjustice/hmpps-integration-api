package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

data class FeatureNotImplementedException(
  val feature: String,
) : RuntimeException("$feature not implemented")
