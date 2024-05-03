package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Needs
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetNeedsForPersonService(
  @Autowired val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<Needs?> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val deliusCrn = personResponse.data["probationOffenderSearch"]?.identifiers?.deliusCrn

    var personNeeds: Response<Needs?> = Response(data = null)

    if (deliusCrn != null) {
      personNeeds = assessRisksAndNeedsGateway.getNeedsForPerson(id = deliusCrn)
    }

    return Response(
      data = personNeeds.data,
      errors = personResponse.errors + personNeeds.errors,
    )
  }
}
