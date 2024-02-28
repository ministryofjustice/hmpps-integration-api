package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class Need(
  val type: String? = null,
  val riskOfHarm: Boolean? = null,
  val riskOfReoffending: Boolean? = null,
  val severity: String? = null,
)
