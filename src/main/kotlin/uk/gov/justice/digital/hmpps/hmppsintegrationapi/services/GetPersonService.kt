package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.CPR_ENABLED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CorePersonRecordGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenderSearchRedirectionResult
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenderSearchResult
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchQuery
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSIdentifierWithPrisonerNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationintegrationepf.LimitedAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService

@Service
class GetPersonService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @Autowired val corePersonRecordGateway: CorePersonRecordGateway,
  @Autowired val featureFlagConfig: FeatureFlagConfig,
  @Autowired val telemetryService: TelemetryService,
  private val deliusGateway: NDeliusGateway,
) {
  fun execute(hmppsId: String): Response<Person?> {
    val probationResponse = getProbationResponse(hmppsId)
    if (identifyHmppsId(hmppsId) == IdentifierType.NOMS && probationResponse.data == null) {
      val prisonResponse = prisonerOffenderSearchGateway.getPrisonOffender(hmppsId)
      return Response(data = prisonResponse.data?.toPerson(), prisonResponse.errors)
    } else {
      return Response(data = probationResponse.data, errors = probationResponse.errors)
    }
  }

  /**
   * This function should always return an identifier.
   * Any exceptions caused by upstream errors will be thrown upstream
   */
  fun convert(
    identifierType: IdentifierType,
    hmppsId: String,
  ): String {
    val cprType =
      when (identifyHmppsId(hmppsId)) {
        identifierType -> return hmppsId
        IdentifierType.UNKNOWN -> throw IllegalArgumentException("Invalid identifier $hmppsId")
        IdentifierType.NOMS -> "prison"
        IdentifierType.CRN -> "probation"
      }
    val cpr = corePersonRecordGateway.corePersonRecordFor(cprType, hmppsId)
    return cpr.getIdentifier(identifierType) ?: throw EntityNotFoundException("No single $identifierType found for $hmppsId in core person record")
  }

  fun getPersonWithPrisonFilter(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<Person?> {
    // Error if not a valid id
    val hmppsIdType = identifyHmppsId(hmppsId)
    if (hmppsIdType == IdentifierType.UNKNOWN) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.BAD_REQUEST)),
      )
    }

    // Get a delius person, to get NOMIS number and for response
    val probationResponse = getProbationResponse(hmppsId)

    if (probationResponse.errors.isNotEmpty() && !probationResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      return Response(
        data = null,
        errors = probationResponse.errors,
      )
    }
    val personOnProbation = probationResponse.data
    val nomisNumber =
      if (personOnProbation?.identifiers?.nomisNumber != null) {
        personOnProbation.identifiers.nomisNumber
      } else if (hmppsIdType == IdentifierType.NOMS) {
        hmppsId
      } else {
        return Response(
          data = null,
          errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)),
        )
      }

    // Get the NOMIS person for prison ID and for verifying exist in NOMIS
    val prisonerResponse = prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)
    if (prisonerResponse.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = prisonerResponse.errors,
      )
    }

    // Filter on Prison
    if (filters?.prisons != null) {
      val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<Person>(prisonerResponse.data?.prisonId, filters)
      if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
        return consumerPrisonFilterCheck
      }
    }

    return Response(data = personOnProbation ?: prisonerResponse.data?.toPerson())
  }

  enum class IdentifierType {
    NOMS,
    CRN,
    UNKNOWN,
  }

  fun identifyHmppsId(input: String): IdentifierType {
    val nomsPattern = Regex("^[A-Z]\\d{4}[A-Z]{2}$")
    val crnPattern = Regex("^[A-Z]{1,2}\\d{6}$")

    return when {
      nomsPattern.matches(input) -> IdentifierType.NOMS
      crnPattern.matches(input) -> IdentifierType.CRN
      else -> IdentifierType.UNKNOWN
    }
  }

  /**
   * Returns a Nomis number from a HMPPS ID
   */
  fun getNomisNumber(hmppsId: String): Response<NomisNumber?> = getNomisNumberWithPrisonFilter(hmppsId, filters = null)

  /**
   * Returns a Nomis number from a HMPPS ID, taking into account prison filters
   */
  fun getNomisNumberWithPrisonFilter(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<NomisNumber?> {
    val nomisNumber =
      when (val idType = identifyHmppsId(hmppsId)) {
        IdentifierType.NOMS -> {
          // No conversion required - but still need to check prisoner with nomisNumber exists
          val prisoner = prisonerOffenderSearchGateway.getPrisonOffender(hmppsId)
          if (prisoner.errors.isNotEmpty()) {
            return Response(data = null, errors = prisoner.errors)
          }
          hmppsId
        }
        else -> {
          // Set the nomis number from CPR if no errors and feature flag enabled
          val cprNomis =
            if (featureFlagConfig.isEnabled(CPR_ENABLED)) {
              runCatching { convert(IdentifierType.NOMS, hmppsId) }
                .onFailure { telemetryService.trackEvent("CPRNomsFailure", mapOf("message" to "Failed to use CPR to convert $hmppsId", "error" to it.message)) }
                .onSuccess { telemetryService.trackEvent("CPRNomsSuccess", mapOf("message" to "Successfully used CPR to convert $hmppsId to $it", "fromId" to hmppsId, "toId" to it)) }
                .getOrNull()
            } else {
              null
            }

          if (cprNomis == null) {
            // Fallback to existing processing
            val legacyConvert = legacyCrnConvert(idType, hmppsId)
            legacyConvert.data?.nomisNumber ?: return legacyConvert
          } else {
            cprNomis
          }
        }
      }
    if (filters?.prisons != null) {
      val prisoner = prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)
      if (prisoner.errors.isNotEmpty()) {
        return Response(data = null, errors = prisoner.errors)
      }

      val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<NomisNumber>(prisoner.data?.prisonId, filters)
      if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
        return consumerPrisonFilterCheck
      }
    }
    return Response(
      data = NomisNumber(nomisNumber),
    )
  }

  /**
   * Get Nomis number from CRN using personFromProbationOffenderSearch
   */
  fun legacyCrnConvert(
    identifierType: IdentifierType,
    crn: String,
  ): Response<NomisNumber?> {
    if (identifierType == IdentifierType.UNKNOWN) {
      return Response(
        data = null,
        errors =
          listOf(
            UpstreamApiError(
              description = "Invalid HMPPS ID: $crn",
              type = UpstreamApiError.Type.BAD_REQUEST,
              causedBy = UpstreamApi.PRISON_API,
            ),
          ),
      )
    }
    val personFromProbationOffenderSearch = getProbationResponse(crn)
    if (personFromProbationOffenderSearch.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = personFromProbationOffenderSearch.errors,
      )
    }
    val nomisNumber = personFromProbationOffenderSearch.data?.identifiers?.nomisNumber
    if (nomisNumber == null) {
      return Response(
        data = null,
        errors =
          listOf(
            UpstreamApiError(
              description = "NOMIS number not found",
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.NDELIUS,
            ),
          ),
      )
    }
    return Response(data = NomisNumber(nomisNumber))
  }

  fun getCombinedDataForPerson(hmppsId: String): Response<OffenderSearchResponse?> {
    val probationResponse = getProbationResponse(hmppsId)

    val prisonResponse =
      (probationResponse.data?.identifiers?.nomisNumber ?: hmppsId.takeIf { identifyHmppsId(it) == IdentifierType.NOMS })
        ?.let { nomsNumber -> prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber) }

    val combinedErrors: List<UpstreamApiError> = probationResponse.errors + (prisonResponse?.errors ?: emptyList())

    if (
      combinedErrors.any { it.type == UpstreamApiError.Type.ENTITY_NOT_FOUND } &&
      !combinedErrors.any { it.type == UpstreamApiError.Type.BAD_REQUEST }
    ) {
      findPrisonerIdMerged(hmppsId)?.let { posIdentifier ->
        return Response(
          data =
            OffenderSearchRedirectionResult(
              prisonerNumber = posIdentifier.prisonerNumber,
              redirectUrl = "/v1/persons/${posIdentifier.prisonerNumber}",
              removedPrisonerNumber = posIdentifier.identifier.value,
            ),
          errors = combinedErrors,
        )
      }
    }

    val probationData = probationResponse.data
    val prisonData = prisonResponse?.data?.toPerson()
    val data: OffenderSearchResponse? =
      if (probationData == null && prisonData == null) null else OffenderSearchResult(prisonData, probationData)
    return Response(data = data, errors = combinedErrors)
  }

  private fun findPrisonerIdMerged(hmppsId: String): POSIdentifierWithPrisonerNumber? {
    val attributeSearchRequest =
      POSAttributeSearchRequest(
        joinType = "AND",
        queries =
          listOf(
            POSAttributeSearchQuery(
              joinType = "AND",
              matchers =
                listOf(
                  POSAttributeSearchMatcher(
                    type = "String",
                    attribute = "identifiers.type",
                    condition = "IS",
                    searchTerm = "MERGED",
                  ),
                  POSAttributeSearchMatcher(
                    type = "String",
                    attribute = "identifiers.value",
                    condition = "IS",
                    searchTerm = hmppsId,
                  ),
                ),
            ),
          ),
      )

    val response =
      prisonerOffenderSearchGateway.attributeSearch(attributeSearchRequest)

    return response.data
      ?.content
      ?.firstOrNull()
      ?.identifiers
      ?.firstOrNull { it.type == "MERGED" && it.value == hmppsId }
      ?.let { identifier ->
        response.data.content
          .firstOrNull()
          ?.prisonerNumber
          ?.let { prisonerNumber -> POSIdentifierWithPrisonerNumber(prisonerNumber, identifier) }
      }
  }

  fun getPersonFromNomis(nomisNumber: String) = prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)

  fun getPrisoner(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<PersonInPrison?> {
    val prisonerNomisNumber = getNomisNumber(hmppsId)

    if (prisonerNomisNumber.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = prisonerNomisNumber.errors,
      )
    }

    val nomisNumber = prisonerNomisNumber.data?.nomisNumber

    val prisonResponse =
      try {
        getPersonFromNomis(nomisNumber!!)
      } catch (e: RuntimeException) {
        if (nomisNumber == null) {
          return Response(
            data = null,
            errors = prisonerNomisNumber.errors,
          )
        }
        return Response(
          data = null,
          errors = listOf(UpstreamApiError(description = e.message ?: "Service error", type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR, causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH)),
        )
      }

    if (prisonResponse.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = prisonResponse.errors,
      )
    }

    val posPrisoner = prisonResponse.data

    if (
      filters != null && !filters.matchesPrison(posPrisoner?.prisonId)
    ) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")),
      )
    }

    return Response(
      data = posPrisoner?.toPersonInPrison(),
      errors = prisonResponse.errors,
    )
  }

  fun getAccessLimitations(hmppsId: String): Response<LimitedAccess?> =
    with(deliusGateway.getAccessLimitations(hmppsId)) {
      Response(
        data = data,
        errors = errors,
      )
    }

  private fun getProbationResponse(hmppsId: String) = deliusGateway.getPerson(hmppsId)
}
