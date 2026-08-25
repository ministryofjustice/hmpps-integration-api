package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr

data class CPRIdentifier(
  val crn: String? = null,
  val prisonNumber: String? = null,
  val defendantId: String? = null,
  val cid: String? = null,
  val pncs: List<String> = emptyList(),
  val cros: List<String> = emptyList(),
  val nationalInsuranceNumbers: List<String> = emptyList(),
  val driverLicenseNumbers: List<String> = emptyList(),
  val arrestSummonsNumbers: List<String> = emptyList(),
  val otherIdentifiers: List<String> = emptyList(),
)
