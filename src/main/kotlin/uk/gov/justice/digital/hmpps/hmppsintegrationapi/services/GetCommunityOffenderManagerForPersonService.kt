package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

class GetCommunityOffenderManagerForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val nDeliusGateway: NDeliusGateway,
) {
  fun execute(hmppsId: String): Response<CommunityOffenderManager?> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)

    val deliusCrn = personResponse.data?.identifiers?.deliusCrn
    var nDeliusMappaDetailResponse: Response<CommunityOffenderManager?> = Response(data = CommunityOffenderManager())

    if (deliusCrn != null) {
      nDeliusMappaDetailResponse = nDeliusGateway.getCommunityOffenderManagerForPerson(id = deliusCrn)
    }

    return Response(
      data = nDeliusMappaDetailResponse.data,
      errors = personResponse.errors + nDeliusMappaDetailResponse.errors,
    )
  }
}
