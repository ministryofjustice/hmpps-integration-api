package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.RiskManagementGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskManagementPlan
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService.IdentifierType

@Service
class GetRiskManagementPlansService(
  @Autowired val riskManagementGateway: RiskManagementGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<List<RiskManagementPlan>?> {
    val crnResponse = getPersonService.convert(hmppsId, IdentifierType.CRN)
    if (crnResponse.data == null) {
      return Response(data = null, errors = crnResponse.errors)
    }

    val crnPlansResponse = riskManagementGateway.getRiskManagementPlansForCrn(crnResponse.data)

    return Response(
      data = crnPlansResponse.data?.toRiskManagementPlans(),
      errors = crnPlansResponse.errors,
    )
  }
}
