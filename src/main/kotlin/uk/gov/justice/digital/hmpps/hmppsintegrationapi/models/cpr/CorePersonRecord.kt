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

data class CorePersonRecordSearchRequest(
  val firstName: String? = null,
  val lastName: String? = null,
  val middleName: String? = null,
  val dateOfBirth: String? = null,
  val firstNameAliases: List<String>? = null,
  val lastNameAliases: List<String>? = null,
  val dateOfBirthAliases: List<String>? = null,
  val postcodes: List<String>? = null,
) {
  fun toMap(): Map<String, Any?> =
    mapOf(
      "firstName" to firstName,
      "lastName" to lastName,
      "middleName" to middleName,
      "dateOfBirth" to dateOfBirth,
      "firstNameAliases" to firstNameAliases,
      "lastNameAliases" to lastNameAliases,
      "dateOfBirthAliases" to dateOfBirthAliases,
      "postcodes" to postcodes,
    )

  fun toAuditableMap(): Map<String, String?> =
    mapOf(
      "firstName" to firstName,
      "lastName" to lastName,
      "middleName" to middleName,
      "dateOfBirth" to dateOfBirth,
    )
}

data class CorePersonRecordSearchResponse(
  val data: List<CorePersonRecordSearchResponseItem>,
)

data class CorePersonRecordSearchResponseItem(
  val name: CPRName? = null,
  val aliases: List<CPRAlias> = emptyList(),
  val addresses: List<CPRAddress> = emptyList(),
  val identifiers: CPRIdentifier? = null,
  val sourceSystem: String? = null,
  val status: String? = null,
  val linkedRecords: List<CorePersonLinkedRecord> = emptyList(),
)

data class CorePersonLinkedRecord(
  val name: CPRName? = null,
  val aliases: List<CPRAlias> = emptyList(),
  val addresses: List<CPRAddress> = emptyList(),
  val identifiers: CPRIdentifier? = null,
  val sourceSystem: String? = null,
  val status: String? = null,
)

data class CPRName(
  val firstName: String? = null,
  val middleNames: String? = null,
  val lastName: String? = null,
  val dateOfBirth: String? = null,
  val aliases: List<CPRAlias> = emptyList(),
)

data class CPRAlias(
  val firstName: String? = null,
  val lastName: String? = null,
  val middleNames: String? = null,
  val title: CPRTitle? = null,
  val sex: CPRSex? = null,
  val addresses: List<CPRAddress>? = null,
)

data class CPRTitle(
  val code: String? = null,
  val description: String? = null,
)

data class CPRSex(
  val code: String? = null,
  val description: String? = null,
)

data class CPRAddress(
  val cprAddressId: String? = null,
  val noFixedAbode: Boolean? = null,
  val startDate: String? = null,
  val startDateTime: String? = null,
  val endDate: String? = null,
  val endDateTime: String? = null,
  val postcode: String? = null,
  val subBuildingName: String? = null,
  val buildingName: String? = null,
  val buildingNumber: String? = null,
  val thoroughfareName: String? = null,
  val dependentLocality: String? = null,
  val postTown: String? = null,
  val county: String? = null,
  val country: String? = null,
  val countryCode: String? = null,
  val uprn: String? = null,
  val status: CPRAddressStatus? = null,
  val comment: String? = null,
  val typeVerified: Boolean? = null,
  val usages: List<CPRAddressUsage>? = null,
  val contacts: List<CPRAddressContact>? = null,
)

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

data class CPRAddressContact(
  val type: CPRAddressContactType? = null,
  val value: String? = null,
  val extension: String? = null,
)

data class CPRAddressContactType(
  val code: String? = null,
  val description: String? = null,
)

data class CPRAddressUsage(
  val code: String? = null,
  val description: String? = null,
  val isActive: Boolean? = null,
)

data class CPRAddressStatus(
  val code: String? = null,
  val description: String? = null,
)
