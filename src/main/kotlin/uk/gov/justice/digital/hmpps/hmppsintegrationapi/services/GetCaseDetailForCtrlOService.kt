package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseDetail

@Service
class GetCaseDetailForCtrlOService(
  @Autowired val nDeliusGateway: NDeliusGateway,
) {
  fun execute(hmppsId: String, eventNumber: Int): Response<CaseDetail?> {
    val caseDetail = nDeliusGateway.getCaseDetailForPerson(hmppsId, eventNumber)

    return Response(
      data = caseDetail.data,
      errors = caseDetail.errors,
    )
  }
}
