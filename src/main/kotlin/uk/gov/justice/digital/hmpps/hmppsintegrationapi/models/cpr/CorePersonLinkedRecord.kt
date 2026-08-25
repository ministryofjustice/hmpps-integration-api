package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr

data class CorePersonLinkedRecord(
  val name: CPRName? = null,
  val aliases: List<CPRAlias> = emptyList(),
  val addresses: List<CPRAddress> = emptyList(),
  val identifiers: CPRIdentifier? = null,
  val sourceSystem: String? = null,
  val status: String? = null,
)
