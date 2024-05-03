package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MappaDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetMappaDetailForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val nDeliusGateway: NDeliusGateway,
) {
  fun execute(hmppsId: String): Response<MappaDetail?> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)

    val deliusCrn = personResponse.data["probationOffenderSearch"]?.identifiers?.deliusCrn
    var nDeliusMappaDetailResponse: Response<MappaDetail?> = Response(data = MappaDetail())

    if (deliusCrn != null) {
      nDeliusMappaDetailResponse = nDeliusGateway.getMappaDetailForPerson(id = deliusCrn)
    }

    return Response(
      data = nDeliusMappaDetailResponse.data,
      errors = nDeliusMappaDetailResponse.errors,
    )
  }
}
