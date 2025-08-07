package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

data class POSIdentifierWithPrisonerNumber(
  val prisonerNumber: String,
  val identifier: POSIdentifier,
)

data class POSIdentifier(
  val type: String?,
  val value: String?,
  val issuedDate: String?,
  val createdDateTime: String?,
)
