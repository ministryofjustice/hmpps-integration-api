package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr

data class CPRAlias(
  val firstName: String? = null,
  val lastName: String? = null,
  val middleNames: String? = null,
  val title: CPRTitle? = null,
  val sex: CPRSex? = null,
  val addresses: List<CPRAddress>? = null,
)
