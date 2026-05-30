package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PhysicalCharacteristics
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@Service
class GetPhysicalCharacteristicsForPersonService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    requestContext: RequestContext? = null,
  ): Response<PhysicalCharacteristics?> {
    val personResponse = getPersonService.getNomisNumber(hmppsId = hmppsId, requestContext = requestContext)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber = personResponse.data?.nomisNumber ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)))

    val prisonerOffenderSearchResponse = prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber, requestContext)
    if (prisonerOffenderSearchResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = prisonerOffenderSearchResponse.errors)
    }

    return Response(
      data = prisonerOffenderSearchResponse.data?.toPhysicalCharacteristics(),
    )
  }
}
