package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManagePOMCaseGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetPrisonOffenderManagerForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val managePOMCaseGateway: ManagePOMCaseGateway,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<PrisonOffenderManager> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)

    val nomisNumber = personResponse.data?.nomisNumber
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
