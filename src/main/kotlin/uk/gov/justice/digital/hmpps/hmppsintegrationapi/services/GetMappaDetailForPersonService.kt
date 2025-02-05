package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MappaDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@Service
class GetMappaDetailForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val nDeliusGateway: NDeliusGateway,
) {
  fun execute(hmppsId: String): Response<MappaDetail?> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)

    if (personResponse.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = personResponse.errors,
      )
    }

    val deliusCrn = personResponse.data?.identifiers?.deliusCrn
    var nDeliusMappaDetailResponse: Response<MappaDetail?> = Response(data = MappaDetail())

    if (deliusCrn != null) {
      nDeliusMappaDetailResponse = nDeliusGateway.getMappaDetailForPerson(id = deliusCrn)

      if (nDeliusMappaDetailResponse.data != null) {
        val mappaDetail = nDeliusMappaDetailResponse.data!!
        if (isNotPopulated(mappaDetail)) {
          nDeliusMappaDetailResponse.errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NDELIUS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND))
        }
      }
    }

    return Response(
      data = nDeliusMappaDetailResponse.data,
      errors = nDeliusMappaDetailResponse.errors,
    )
  }

  private fun isNotPopulated(ndeliusMappaData: MappaDetail?): Boolean = ndeliusMappaData?.level == null && ndeliusMappaData?.levelDescription == null && ndeliusMappaData?.category == null && ndeliusMappaData?.categoryDescription == null && ndeliusMappaData?.startDate == null && ndeliusMappaData?.reviewDate == null && ndeliusMappaData?.notes == null
}
