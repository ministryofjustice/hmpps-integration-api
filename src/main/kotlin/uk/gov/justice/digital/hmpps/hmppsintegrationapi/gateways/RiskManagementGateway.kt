package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways;

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.CrnRiskManagementPlans

class RiskManagementGateway {

  fun getRiskManagementPlansForCrn(crn: String): CrnRiskManagementPlans {
    return CrnRiskManagementPlans(crn, "plan", emptyList())
  }
}
