package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.Supervision

@Service
class GetEventsForCtrlOService(
  @Autowired val nDeliusGateway: NDeliusGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(deliusCrn: String): Response<List<Supervision>> {
    val personResponse = getPersonService.execute(hmppsId = deliusCrn)
    val deliusCrn = personResponse.data?.identifiers?.deliusCrn
    var nDeliusSupervisions: Response<List<Supervision>> = Response(data = emptyList())

    if (deliusCrn != null) {
      nDeliusSupervisions = nDeliusGateway.getSupervisionsForPerson(deliusCrn)
    }

    return Response(
      data = nDeliusSupervisions.data,
      errors = personResponse.errors + nDeliusSupervisions.errors,
    )
  }
}
