package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.CrnRiskManagementPlans

@Component
class RiskManagementGateway (
  @Value("\${risk-management-plan-search.base-url}") baseUrl: String,
){

  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getRiskManagementPlansForCrn(crn: String): CrnRiskManagementPlans {
    return CrnRiskManagementPlans(crn, "plan", emptyList())
  }
}
