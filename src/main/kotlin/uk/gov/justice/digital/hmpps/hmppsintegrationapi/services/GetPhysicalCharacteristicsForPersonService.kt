package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PhysicalCharacteristics
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetPhysicalCharacteristicsForPersonService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters? = null,
  ): Response<PhysicalCharacteristics?> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters = filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber = personResponse.data?.nomisNumber ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND)))

    val prisonerOffenderSearchResponse = prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)
    if (prisonerOffenderSearchResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = prisonerOffenderSearchResponse.errors)
    }

    return Response(
      data = prisonerOffenderSearchResponse.data?.toPhysicalCharacteristics(),
    )
  }
}
