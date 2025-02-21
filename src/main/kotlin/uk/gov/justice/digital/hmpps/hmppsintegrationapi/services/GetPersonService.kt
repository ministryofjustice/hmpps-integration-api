package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonOnProbation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetPersonService(
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(hmppsId: String): Response<Person?> {
    val probationResponse = probationOffenderSearchGateway.getPerson(id = hmppsId)
    if (identifyHmppsId(hmppsId) == IdentifierType.NOMS && probationResponse.data == null) {
      val prisonResponse = prisonerOffenderSearchGateway.getPrisonOffender(hmppsId)
      return Response(data = prisonResponse.data?.toPerson(), prisonResponse.errors)
    } else {
      return Response(data = probationResponse.data, errors = probationResponse.errors)
    }
  }

  fun getPersonWithPrisonFilter(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<Person?> {
    var personOnProbation: PersonOnProbation? = null

    // 1. Is it NOMIS No. shaped
    var nomisNumber: String?
    if (identifyHmppsId(hmppsId) == IdentifierType.NOMS) {
      nomisNumber = hmppsId
    } else {
      val probationResponse = probationOffenderSearchGateway.getPerson(id = hmppsId)
      if (probationResponse.errors.isNotEmpty() && !probationResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
        return Response(
          data = null,
          errors = probationResponse.errors,
        )
      }
      personOnProbation = probationResponse.data
      nomisNumber = personOnProbation?.identifiers?.nomisNumber
    }

    var prisoner: POSPrisoner? = null

    // 3. If filters get the prison id from a prison search and filter based on it
    if (filters?.prisons != null) {
      if (nomisNumber == null) {
        return Response(
          data = null,
          errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.BAD_REQUEST)),
        )
      }
      val prisonerResponse = prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)
      if (prisonerResponse.errors.isNotEmpty()) {
        return Response(
          data = null,
          errors = prisonerResponse.errors,
        )
      }
      prisoner = prisonerResponse.data
      val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<Person>(prisoner?.prisonId, filters)
      if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
        return consumerPrisonFilterCheck
      }
    }

    // 2. Go to probation offender search to try and find the hmppsId
    if (personOnProbation == null) {
      val probationResponse = probationOffenderSearchGateway.getPerson(id = hmppsId)
      if (probationResponse.errors.isNotEmpty() && !probationResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
        return Response(
          data = null,
          errors = probationResponse.errors,
        )
      }
      personOnProbation = probationResponse.data
      nomisNumber = personOnProbation?.identifiers?.nomisNumber
    }
    if (personOnProbation != null) {
      return Response(data = personOnProbation)
    }

    // 2a. If they don't have one, use prison search to verify that they exist
    // 3a. If we already did a search can we avoid calling prison search again?
    if (prisoner == null) {
      if (nomisNumber == null) {
        return Response(
          data = null,
          errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.BAD_REQUEST)),
        )
      }
      val prisonerResponse = prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)
      if (prisonerResponse.errors.isNotEmpty()) {
        return Response(
          data = null,
          errors = prisonerResponse.errors,
        )
      }
      prisoner = prisonerResponse.data
    }
    if (prisoner != null) {
      return Response(data = prisoner.toPerson())
    }

    return Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)))
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
   * Identify whether the hmppsId is a noms number or a crn
   * When it is a noms number then return it.
   * When it is a CRN look up the prisoner in probation offender search and then return it
   */
  fun getNomisNumber(hmppsId: String): Response<NomisNumber?> =
    when (identifyHmppsId(hmppsId)) {
      IdentifierType.NOMS -> {
        val prisoner = prisonerOffenderSearchGateway.getPrisonOffender(hmppsId)
        if (prisoner.errors.isNotEmpty()) {
          Response(data = null, errors = prisoner.errors)
        } else {
          Response(data = NomisNumber(hmppsId))
        }
      }

      IdentifierType.CRN -> {
        val personFromProbationOffenderSearch = probationOffenderSearchGateway.getPerson(id = hmppsId)
        val nomisNumber = personFromProbationOffenderSearch.data?.identifiers?.nomisNumber
        val errors = personFromProbationOffenderSearch.errors.toMutableList()

        if (nomisNumber == null) {
          errors.add(
            UpstreamApiError(
              description = "NOMIS number not found",
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
            ),
          )
        }

        Response(
          data = nomisNumber?.let { NomisNumber(it) },
          errors = errors,
        )
      }

      IdentifierType.UNKNOWN ->
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                description = "Invalid HMPPS ID: $hmppsId",
                type = UpstreamApiError.Type.BAD_REQUEST,
                causedBy = UpstreamApi.NOMIS,
              ),
            ),
        )
    }

  fun getCombinedDataForPerson(hmppsId: String): Response<OffenderSearchResponse> {
    val probationResponse = probationOffenderSearchGateway.getPerson(id = hmppsId)

    val prisonResponse =
      probationResponse.data?.identifiers?.nomisNumber?.let {
        prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = it)
      }

    val data =
      OffenderSearchResponse(
        prisonerOffenderSearch = prisonResponse?.data?.toPerson(),
        probationOffenderSearch = probationResponse.data,
      )

    return Response(
      data = data,
      errors = (prisonResponse?.errors ?: emptyList()) + probationResponse.errors,
    )
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
}

fun isNomsNumber(id: String?): Boolean = id?.matches(Regex("^[A-Z]\\d{4}[A-Z]{2}+$")) == true
