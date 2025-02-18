package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetHmppsIdService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    nomisNumber: String,
    filters: ConsumerFilters? = null,
  ): Response<HmppsId?> {
    val identifierType = getPersonService.identifyHmppsId(nomisNumber)
    if (identifierType != GetPersonService.IdentifierType.NOMS) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.BAD_REQUEST)),
      )
    }

    val personResponse = getPersonService.execute(nomisNumber.uppercase())
    if (personResponse.errors.isNotEmpty() && !personResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      return Response(
        data = null,
        errors = personResponse.errors,
      )
    }
    var hmppsId = personResponse.data?.hmppsId
    // If no filters passed in, then we don't need to get the prison ID
    if (hmppsId != null && filters == null) {
      return Response(data = HmppsId(hmppsId))
    }

    val (prisoner, prisonerErrors) = getPersonService.getPersonFromNomis(nomisNumber.uppercase())
    if (prisonerErrors.isNotEmpty()) {
      return Response(
        data = null,
        errors = prisonerErrors,
      )
    }

    if (hmppsId == null) {
      hmppsId = prisoner?.prisonerNumber
      if (hmppsId == null) {
        return Response(
          data = null,
          errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)),
        )
      }
    }

    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsId>(prisoner?.prisonId, filters)
    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    return Response(
      data = HmppsId(hmppsId),
    )
  }

  fun getNomisNumber(hmppsId: String): Response<NomisNumber?> {
    val nomisResponse = getPersonService.getNomisNumber(hmppsId = hmppsId)

    return Response(
      data = NomisNumber(nomisNumber = nomisResponse.data?.nomisNumber),
      errors = nomisResponse.errors,
    )
  }
}
