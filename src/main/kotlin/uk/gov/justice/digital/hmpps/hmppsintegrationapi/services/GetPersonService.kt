package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.CPR_ENABLED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.CprResultException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FilterViolationException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ForbiddenByUpstreamServiceException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.UpstreamApiException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CorePersonRecordGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenderSearchRedirectionResult
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenderSearchResult
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonOnProbation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchQuery
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSIdentifierWithPrisonerNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
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
   * Converts an hmppsId to the required person id using either CPR or the probation or prison APIs
   * Either a person on probation id or a person in prison id.
   *
   */
  fun convert(
    hmppsId: String,
    requiredType: IdentifierType,
  ): Response<String?> {
    val hmppsIdType = identifyHmppsId(hmppsId)
    if (hmppsIdType == IdentifierType.UNKNOWN) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.BAD_REQUEST, description = "Invalid HMPPS ID: $hmppsId")),
      )
    }

    // Return the id if the hmppsIdType is the same as the required type
    if (hmppsIdType == requiredType) {
      return Response(hmppsId)
    }

    return getIdFromCprOrPrisonOrProbationSystems(hmppsId, hmppsIdType, requiredType)
  }

  /**
   * Verifies that a given id exists in its own domain indicated by its format (whether it is a NOMS number or CRN)
   *
   */
  fun verifyId(id: String): Response<String?> {
    val hmppsIdType = identifyHmppsId(id)
    if (hmppsIdType == IdentifierType.UNKNOWN) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.BAD_REQUEST, description = "Invalid HMPPS ID: $id")),
      )
    }
    return getIdFromCprOrPrisonOrProbationSystems(id, hmppsIdType, hmppsIdType)
  }

  fun trackCPRFailureEvent(
    exception: Throwable,
    hmppsId: String,
    fallbackId: String? = null,
    fallbackErrors: List<UpstreamApiError> = emptyList(),
  ) {
    val event =
      when (exception) {
        is CprResultException -> if (exception.multipleIds.isNotEmpty()) "CPRNomsMultipleMatches" else "CPRNomsNoMatches"
        is EntityNotFoundException -> "CPRNomsNotFound"
        else -> "CPRNomsFailure"
      }
    val properties =
      listOfNotNull(
        "fallbackSuccess" to (fallbackId != null).toString(),
        if (fallbackId != null) "fallbackId" to fallbackId else null,
        if (fallbackErrors.isNotEmpty()) "fallbackErrors" to fallbackErrors.mapNotNull { it.description }.joinToString(",") else null,
        "message" to "Failed to use CPR to convert $hmppsId",
        "error" to exception.message,
      ).toMap()

    telemetryService.trackEvent(event, properties)
  }

  /**
   * Refactored existing processing to get a personId from either a prisoner id or a probation id starting from the prison domain
   * Also verifies that the person exists in the probation api
   */
  private fun prisonAPIPersonId(
    nomisNumber: String,
    requiredType: IdentifierType,
  ): Response<String?> {
    val prisoner = prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)
    if (prisoner.errors.isNotEmpty()) {
      return Response(data = null, errors = prisoner.errors)
    }
    return if (requiredType == IdentifierType.NOMS) {
      Response(nomisNumber)
    } else {
      val personOnProbation = deliusGateway.getOffender(nomisNumber)
      if (personOnProbation.errors.isNotEmpty()) {
        return Response(data = null, errors = personOnProbation.errors)
      }
      Response(personOnProbation.data?.otherIds?.crn!!)
    }
  }

  /**
   * Refactored existing processing to get a personId from either a prisoner id or a probation id starting from the probation domain
   * Also verifies that the person exists in the probation api
   */
  private fun probationAPIPersonId(
    crn: String,
    requiredType: IdentifierType,
  ): Response<String?> {
    val personOnProbation = getProbationResponse(crn)
    if (personOnProbation.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = personOnProbation.errors,
      )
    }
    val nomisNumber = personOnProbation.data?.identifiers?.nomisNumber
    if (nomisNumber == null && requiredType == IdentifierType.NOMS) {
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
    return Response(if (requiredType == IdentifierType.NOMS) nomisNumber!! else crn)
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
   * Returns a Nomis number from a HMPPS ID, taking into account optionally provided prison and supervision status filters
   * If the prisoner isn't found or their current location or supervision status doesn't match the consumer's
   * filters, a NOT_FOUND error response is returned
   */
  fun getNomisNumber(
    hmppsId: String,
    filters: ConsumerFilters? = null,
  ): Response<NomisNumber?> {
    val id = convert(hmppsId, IdentifierType.NOMS)
    val nomisNumber = id.data ?: return Response(data = null, errors = id.errors)

    ensurePermittedPrisonerLocation(nomisNumber, filters)
    ensurePermittedSupervisionStatus(nomisNumber, filters)

    return Response(
      data = NomisNumber(nomisNumber),
    )
  }

  private fun ensurePermittedSupervisionStatus(
    nomisId: String,
    filters: ConsumerFilters?,
  ) {
    when {
      filters?.hasSupervisionStatusesFilter() != true -> return
      filters.supervisionStatuses!!.containsAll(setOf("PRISONS", "PROBATION", "NONE")) -> return
      !filters.supervisionStatuses.contains(getPersonSupervisionStatus(nomisId)) -> {
        throw FilterViolationException("SupervisionStatus filter restricts access to the requested prisoner's supervision status")
      }
    }
  }

  private fun getPersonSupervisionStatus(nomisId: String): String {
    val status = getPersonFromPrisonerOffenderSearch(nomisId)?.status ?: return "UNKNOWN"

    if (status.startsWith("ACTIVE")) {
      return "PRISONS"
    }
    val probationData = getPersonFromDelius(nomisId, true)
    return when (probationData.data?.underActiveSupervision) {
      true -> "PROBATION"
      false -> "NONE"
      else -> "UNKNOWN"
    }
  }

  private fun ensurePermittedPrisonerLocation(
    nomisId: String,
    filters: ConsumerFilters?,
  ) {
    if (filters?.hasPrisonFilter() != true) return
    val prisoner = getPersonFromPrisonerOffenderSearch(nomisId)

    val prisonId = prisoner?.prisonId

    if (prisonId == null || consumerPrisonAccessService.checkConsumerHasPrisonAccess<NomisNumber>(prisonId, filters).errors.isNotEmpty()) {
      throw FilterViolationException("PrisonFilter restricts access to the requested prisoner's location")
    }
  }

  fun getCombinedDataForPerson(
    hmppsId: String,
    filters: ConsumerFilters? = null,
  ): Response<OffenderSearchResponse?> {
    val probationResponse = getProbationResponse(hmppsId)

    val prisonerId = hmppsId.takeIf { identifyHmppsId(it) == IdentifierType.NOMS } ?: probationResponse.data?.identifiers?.nomisNumber

    var prisonResponse =
      prisonerId?.let { nomsNumber ->
        prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber)
      }

    var combinedErrors: List<UpstreamApiError> = probationResponse.errors + (prisonResponse?.errors ?: emptyList())

    if (
      prisonerId != null &&
      combinedErrors.any { it.type == UpstreamApiError.Type.ENTITY_NOT_FOUND && it.causedBy == UpstreamApi.PRISONER_OFFENDER_SEARCH } &&
      !combinedErrors.any { it.type == UpstreamApiError.Type.BAD_REQUEST }
    ) {
      findPrisonerIdMerged(prisonerId)?.let { posIdentifier ->
        // If called with a NOMS, return a redirect to the merged prisoner number
        if (identifyHmppsId(hmppsId) == IdentifierType.NOMS) {
          return Response(
            data =
              OffenderSearchRedirectionResult(
                prisonerNumber = posIdentifier.prisonerNumber,
                redirectUrl = "/v1/persons/${posIdentifier.prisonerNumber}",
                removedPrisonerNumber = posIdentifier.identifier.value,
              ),
            errors = combinedErrors,
          )
        } else {
          // Otherwise call the prisoner search again with the merged prisoner number
          prisonResponse = prisonerOffenderSearchGateway.getPrisonOffender(posIdentifier.prisonerNumber)
          combinedErrors = probationResponse.errors + prisonResponse.errors
        }
      }
    }
    val probationData = probationResponse.data

    // If supervisionStatus filter is Probation Only and probation record is NOT under active supervision
    if (filters?.isProbationOnly() == true && probationData?.underActiveSupervision == false) {
      throw ForbiddenByUpstreamServiceException("Not under active supervision. Access denied.")
    }

    // If supervisions filter is NOT empty but does NOT include PRISONS. Then do not return prisons data
    val prisonData =
      if (filters?.hasSupervisionStatusesFilter() == true && filters.hasPrisons() == false) {
        null
      } else {
        prisonResponse?.data?.toPerson()
      }

    val data: OffenderSearchResponse? =
      if (probationData == null && prisonData == null) null else OffenderSearchResult(prisonData, probationData)
    return Response(data = data, errors = combinedErrors)
  }

  private fun findPrisonerIdMerged(prisonerId: String): POSIdentifierWithPrisonerNumber? {
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
                    searchTerm = prisonerId,
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
      ?.firstOrNull { it.type == "MERGED" && it.value == prisonerId }
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

  private fun getProbationResponse(hmppsId: String) = getPersonFromDelius(hmppsId)

  private fun getPersonFromPrisonerOffenderSearch(nomisId: String): POSPrisoner? {
    val searchResponse = prisonerOffenderSearchGateway.getPrisonOffender(nomisId)
    if (searchResponse.errors.isNotEmpty()) {
      throw UpstreamApiException(UpstreamApi.PRISONER_OFFENDER_SEARCH, searchResponse.errors.first().type, "person", nomisId, searchResponse.errors)
    }
    return searchResponse.data
  }

  fun getPersonFromDelius(
    id: String? = null,
    throwErrors: Boolean? = false,
  ): Response<PersonOnProbation?> {
    val offender = deliusGateway.getOffender(id)
    if (throwErrors == true && offender.errors.isNotEmpty()) {
      throw UpstreamApiException(UpstreamApi.NDELIUS, offender.errors.first().type, "person", id, offender.errors)
    }
    return Response(data = offender.data?.toPersonOnProbation(), errors = offender.errors)
  }

  /**
   * Attempts to get the id from CPR
   * Falls back to Prison and Probation APIs if CPR is not successful
   *
   */
  fun getIdFromCprOrPrisonOrProbationSystems(
    hmppsId: String,
    thisIdType: IdentifierType,
    requiredType: IdentifierType,
  ): Response<String?> {
    var cprFailureException: Exception? = null
    if (featureFlagConfig.isEnabled(CPR_ENABLED)) {
      try {
        val cpr = corePersonRecordGateway.corePersonRecordFor(thisIdType, hmppsId)
        val id = cpr.getIdentifier(requiredType, hmppsId)
        telemetryService.trackEvent("CPRNomsSuccess", mapOf("message" to "Successfully used CPR to convert $hmppsId to $id", "fromId" to hmppsId, "toId" to id))
        return Response(cpr.getIdentifier(requiredType, id))
      } catch (ex: Exception) {
        cprFailureException = ex
      }
    }
    // Fall back to using the prison API or probation API to get the person id
    val response =
      when (thisIdType) {
        IdentifierType.NOMS -> prisonAPIPersonId(hmppsId, requiredType)
        else -> probationAPIPersonId(hmppsId, requiredType)
      }
    // Track the CPR exception using the fallback response
    cprFailureException?.let {
      trackCPRFailureEvent(it, hmppsId, response.data, response.errors)
    }
    return response
  }
}
