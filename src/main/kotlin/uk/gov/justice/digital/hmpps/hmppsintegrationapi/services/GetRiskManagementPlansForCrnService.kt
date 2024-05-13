package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.RiskManagementGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskManagementPlan

@Service
class GetRiskManagementPlansForCrnService (
  @Autowired val riskManagementGateway: RiskManagementGateway
){

  fun execute(crn: String): Response<List<RiskManagementPlan>?> {
    val crnPlansResponse = riskManagementGateway.getRiskManagementPlansForCrn(crn)

    return Response(
      data = crnPlansResponse.data?.toRiskManagementPlans(),
      errors = crnPlansResponse.errors
    )

  }

}
