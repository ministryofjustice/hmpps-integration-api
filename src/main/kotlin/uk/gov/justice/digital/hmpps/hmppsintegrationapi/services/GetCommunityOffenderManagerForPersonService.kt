package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetCommunityOffenderManagerForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val nDeliusGateway: NDeliusGateway,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<CommunityOffenderManager> {
    val personResponse = getPersonService.getPersonWithPrisonFilter(hmppsId, filters)

    val deliusCrn = personResponse.data?.identifiers?.deliusCrn
    var nDeliusMappaDetailResponse: Response<CommunityOffenderManager> = Response(data = CommunityOffenderManager())

    if (deliusCrn != null) {
      nDeliusMappaDetailResponse = nDeliusGateway.getCommunityOffenderManagerForPerson(id = deliusCrn)
    }

    return Response(
      data = nDeliusMappaDetailResponse.data,
      errors = personResponse.errors + nDeliusMappaDetailResponse.errors,
    )
  }
}
