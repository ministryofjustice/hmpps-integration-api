package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManagePOMCaseGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonOfficerManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetPrisonOfficerManagerForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val managePOMCaseGateway: ManagePOMCaseGateway,
) {
  fun execute(hmppsId: String): Response<PrisonOfficerManager> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val nomisNumber = personResponse.data?.identifiers?.nomisNumber
    var prisonOfficeManager: Response<PrisonOfficerManager> = Response(data = PrisonOfficerManager())

    if (nomisNumber != null) {
      prisonOfficeManager = managePOMCaseGateway.getPrimaryPOMForNomisNumber(nomisNumber)
    }

    return Response(
      data = prisonOfficeManager.data,
      errors = personResponse.errors + prisonOfficeManager.errors,
    )
  }
}
