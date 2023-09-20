package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Risk

@Service
class GetRisksForPersonService(
  @Autowired val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(pncId: String): Response<Risk?> {
    val personResponse = getPersonService.execute(pncId = pncId)
    val deliusCrn = personResponse.data["probationOffenderSearch"]?.identifiers?.deliusCrn

    var personRisks: Response<Risk?> = Response(data = null)

    if (deliusCrn != null) {
      personRisks = assessRisksAndNeedsGateway.getRisksForPerson(id = deliusCrn)
    }

    return Response(
      data = personRisks.data,
      errors = personResponse.errors + personRisks.errors,
    )
  }
}
