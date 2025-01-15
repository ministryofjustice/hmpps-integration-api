package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.StatusInformation

@Service
class GetStatusInformationForPersonService(
  @Autowired val nDeliusGateway: NDeliusGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<List<StatusInformation>> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val deliusCrn = personResponse.data?.identifiers?.deliusCrn

    var nDeliusPersonStatus: Response<List<StatusInformation>> = Response(data = emptyList())

    if (deliusCrn != null) {
      val allNDeliusPersonStatus = nDeliusGateway.getStatusInformationForPerson(deliusCrn)
      val filteredNDeliusPersonStatus =
        allNDeliusPersonStatus.data.filter {
          it.code in
            listOf(
              "ASFO",
              "WRSM",
            )
        }
      nDeliusPersonStatus = Response(data = filteredNDeliusPersonStatus, errors = allNDeliusPersonStatus.errors)
    }

    return Response(
      data = nDeliusPersonStatus.data,
      errors = personResponse.errors + nDeliusPersonStatus.errors,
    )
  }
}
