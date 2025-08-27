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

    val nomisNumber = response.data?.nomisNumber
    nomisNumber?.let {
      val schedulesResponse = sanGateway.getPlanCreationSchedules(it)

      val updatedSchedules = schedulesResponse.data.planCreationSchedules
        .map { schedule -> schedule.copy(nomisNumber = it) }

      return Response(
        PlanCreationSchedules(updatedSchedules),
        schedulesResponse.errors
      )
    }

    return Response(PlanCreationSchedules(emptyList()), response.errors)
  }
}
