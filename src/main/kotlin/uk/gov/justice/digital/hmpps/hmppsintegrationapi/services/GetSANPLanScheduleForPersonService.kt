package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.SANGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanCreationSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetSANPLanScheduleForPersonService(
  @Autowired val sanGateway: SANGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun getPlanCreationSchedules(hmppsId: String): Response<PlanCreationSchedules> {
    val response = getPersonService.getNomisNumber(hmppsId = hmppsId)

    response.data?.nomisNumber?.let {
      return sanGateway.getPlanCreationSchedules(it)
    }
    return Response(PlanCreationSchedules(listOf()), response.errors)
  }
}
