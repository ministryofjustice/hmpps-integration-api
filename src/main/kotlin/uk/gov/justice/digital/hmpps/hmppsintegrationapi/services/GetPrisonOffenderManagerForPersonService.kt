package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManagePOMCaseGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetPrisonOffenderManagerForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val managePOMCaseGateway: ManagePOMCaseGateway,
) {
  fun execute(hmppsId: String): Response<PrisonOffenderManager> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val nomisNumber = personResponse.data["probationOffenderSearch"]?.identifiers?.nomisNumber
    var prisonOffenderManager: Response<PrisonOffenderManager> = Response(data = PrisonOffenderManager())

    if (nomisNumber != null) {
      prisonOffenderManager = managePOMCaseGateway.getPrimaryPOMForNomisNumber(nomisNumber)
    }

    return Response(
      data = prisonOffenderManager.data,
      errors = personResponse.errors + prisonOffenderManager.errors,
    )
  }
}
