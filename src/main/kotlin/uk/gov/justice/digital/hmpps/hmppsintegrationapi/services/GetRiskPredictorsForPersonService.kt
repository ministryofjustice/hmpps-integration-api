package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictor

@Service
class GetRiskPredictorsForPersonService(
  @Autowired val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(pncId: String): Response<List<RiskPredictor>> {
    val personResponse = getPersonService.execute(pncId = pncId)
    val crn = personResponse.data["probationOffenderSearch"]?.identifiers?.deliusCrn

    var personRiskPredictors: Response<List<RiskPredictor>> = Response(data = emptyList())

    if (crn != null) {
      personRiskPredictors = assessRisksAndNeedsGateway.getRiskPredictorsForPerson(id = crn)
    }

    return Response(
      data = personRiskPredictors.data,
      errors = personResponse.errors + personRiskPredictors.errors,
    )
  }
}
