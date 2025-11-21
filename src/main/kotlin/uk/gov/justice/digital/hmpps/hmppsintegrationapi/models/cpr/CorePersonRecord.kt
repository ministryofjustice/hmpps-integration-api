package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.CprResultException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService.IdentifierType
import java.time.LocalDate

data class CorePersonRecord(
  val cprUUID: String? = null,
  val firstName: String? = null,
  val middleNames: String? = null,
  val lastName: String? = null,
  val dateOfBirth: String? = null,
  val title: Title? = null,
  val sex: Sex? = null,
  val religion: Religion? = null,
  val ethnicity: Ethnicity? = null,
  val aliases: List<Alias> = emptyList(),
  var nationalities: List<Nationality> = emptyList(),
  val addresses: List<Address> = emptyList(),
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
  ) = getIdentifier(identifierType) ?: throw CprResultException(identifierType, hmppsId)

  fun hasMultipleIdentifiersForType(identifierType: IdentifierType) =
    when (identifierType) {
      IdentifierType.NOMS -> identifiers?.prisonNumbers?.let { it.size > 1 } == true
      IdentifierType.CRN -> identifiers?.crns?.let { it.size > 1 } == true
      else -> false
    }
}

data class Title(
  val code: String? = null,
  val description: String? = null,
)

data class Sex(
  val code: String? = null,
  val description: String? = null,
)

data class Religion(
  val code: String? = null,
  val description: String? = null,
)

data class Ethnicity(
  val code: String? = null,
  val description: String? = null,
)

data class Alias(
  val firstName: String? = null,
  val lastName: String? = null,
  val middleNames: String? = null,
  val title: Title? = null,
  val sex: Sex? = null,
)

data class Nationality(
  val code: String? = null,
  val description: String? = null,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val notes: String? = null,
)

data class Address(
  val noFixedAbode: Boolean? = null,
  val startDate: String? = null,
  val endDate: String? = null,
  val postcode: String? = null,
  val subBuildingName: String? = null,
  val buildingName: String? = null,
  val buildingNumber: String? = null,
  val thoroughfareName: String? = null,
  val dependentLocality: String? = null,
  val postTown: String? = null,
  val county: String? = null,
  val country: String? = null,
  val uprn: String? = null,
)

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
