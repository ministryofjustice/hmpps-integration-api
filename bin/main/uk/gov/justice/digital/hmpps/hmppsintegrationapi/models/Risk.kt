package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

data class Risk(
  val risk: String? = null,
  val previous: String? = null,
  val previousConcernsText: String? = null,
  val current: String? = null,
  val currentConcernsText: String? = null,
)
