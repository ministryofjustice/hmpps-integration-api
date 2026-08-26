package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr

data class CPRAddressContact(
  val type: CPRAddressContactType? = null,
  val value: String? = null,
  val extension: String? = null,
)
