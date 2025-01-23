package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetPersonService(
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
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
