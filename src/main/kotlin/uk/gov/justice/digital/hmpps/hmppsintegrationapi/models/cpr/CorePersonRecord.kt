package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.CprResultException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService.IdentifierType

data class CorePersonRecord(
  val identifiers: Identifiers? = null,
) {
  fun getIdentifier(identifierType: IdentifierType): String? =
    when (identifierType) {
      // Only handling cases with exactly one nomisNumber or crn
      IdentifierType.NOMS -> identifiers?.prisonNumbers?.takeIf { it.size == 1 }?.firstOrNull()
      IdentifierType.CRN -> identifiers?.crns?.takeIf { it.size == 1 }?.firstOrNull()
      else -> null
    }

  fun getIdentifier(
    identifierType: IdentifierType,
    hmppsId: String,
  ) = getIdentifier(identifierType) ?: throw CprResultException(identifierType, hmppsId, multipleIdentifiersForType(identifierType))

  fun multipleIdentifiersForType(identifierType: IdentifierType): List<String> =
    when (identifierType) {
      IdentifierType.NOMS -> identifiers?.prisonNumbers?.takeIf { it.size > 1 } ?: emptyList()
      IdentifierType.CRN -> identifiers?.crns?.takeIf { it.size > 1 } ?: emptyList()
      else -> emptyList()
    }
}

data class Identifiers(
  val crns: List<String> = emptyList(),
  val prisonNumbers: List<String> = emptyList(),
  val defendantIds: List<String> = emptyList(),
  val cids: List<String> = emptyList(),
  val pncs: List<String> = emptyList(),
  val cros: List<String> = emptyList(),
  val nationalInsuranceNumbers: List<String> = emptyList(),
  val driverLicenseNumbers: List<String> = emptyList(),
  val arrestSummonsNumbers: List<String> = emptyList(),
)
