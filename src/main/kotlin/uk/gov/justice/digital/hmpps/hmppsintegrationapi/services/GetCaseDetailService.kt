package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.EffectiveProposalFrameworkAndDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response

@Service
class GetCaseDetailService(
  @Autowired val effectiveProposalFrameworkAndDelius: EffectiveProposalFrameworkAndDeliusGateway,
) {
  fun execute(hmppsId: String, eventNumber: Int): Response<CaseDetail?> {
    val caseDetail = effectiveProposalFrameworkAndDelius.getCaseDetailForPerson(hmppsId, eventNumber)

    return Response(
      data = caseDetail.data,
      errors = caseDetail.errors,
    )
  }
}
