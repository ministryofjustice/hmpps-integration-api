package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManagePOMCaseGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters

@Service
class GetPrisonOffenderManagerForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val managePOMCaseGateway: ManagePOMCaseGateway,
) {
  fun execute(
    hmppsId: String,
    filters: RoleFilters?,
  ): Response<PrisonOffenderManager?> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber = personResponse.data?.nomisNumber ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)))

    val prisonOffenderManagerResponse = managePOMCaseGateway.getPrimaryPOMForNomisNumber(nomisNumber)
    if (prisonOffenderManagerResponse.errors.isNotEmpty() && !prisonOffenderManagerResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      return Response(data = null, errors = prisonOffenderManagerResponse.errors)
    }

    return Response(
      data = prisonOffenderManagerResponse.data,
    )
  }
}
