package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risks
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@Service
class GetRiskSeriousHarmForPersonService(
  @Autowired val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<Risks?> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val deliusCrn = personResponse.data?.identifiers?.deliusCrn

    var personRisks: Response<Risks?> = Response(data = null)

    if (deliusCrn != null) {
      personRisks = assessRisksAndNeedsGateway.getRiskSeriousHarmForPerson(id = deliusCrn)
      if (personRisks.hasError(UpstreamApiError.Type.FORBIDDEN)) {
        return Response(data = null, errors = personResponse.errors + personRisks.errors)
      }
    }

    return Response(
      data = personRisks.data,
      errors = personResponse.errors + personRisks.errors,
    )
  }
}
