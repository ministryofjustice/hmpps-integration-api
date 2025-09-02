package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters

@Service
class GetCommunityOffenderManagerForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val nDeliusGateway: NDeliusGateway,
) {
  fun execute(
    hmppsId: String,
    filters: RoleFilters?,
  ): Response<CommunityOffenderManager?> {
    val personResponse = getPersonService.getPersonWithPrisonFilter(hmppsId, filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val deliusCrn = personResponse.data?.identifiers?.deliusCrn ?: return Response(data = null)

    val nDeliusMappaDetailResponse = nDeliusGateway.getCommunityOffenderManagerForPerson(crn = deliusCrn)
    if (nDeliusMappaDetailResponse.errors.isNotEmpty() && !nDeliusMappaDetailResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      return Response(data = null, errors = nDeliusMappaDetailResponse.errors)
    }

    return Response(
      data = nDeliusMappaDetailResponse.data,
    )
  }
}
