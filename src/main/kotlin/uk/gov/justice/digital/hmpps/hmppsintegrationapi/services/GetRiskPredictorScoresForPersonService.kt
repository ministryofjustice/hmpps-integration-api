package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictorScore

@Service
class GetRiskPredictorScoresForPersonService(
  @Autowired val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(pncId: String): Response<List<RiskPredictorScore>> {
    val personResponse = getPersonService.execute(hmppsId = pncId)
    val deliusCrn = personResponse.data?.identifiers?.deliusCrn

    var personRiskPredictorScores: Response<List<RiskPredictorScore>> = Response(data = emptyList())

    if (deliusCrn != null) {
      personRiskPredictorScores = assessRisksAndNeedsGateway.getRiskPredictorScoresForPerson(id = deliusCrn)
    }

    return Response(
      data = personRiskPredictorScores.data,
      errors = personResponse.errors + personRiskPredictorScores.errors,
    )
  }
}
