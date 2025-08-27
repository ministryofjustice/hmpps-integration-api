package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.SANGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanCreationSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanReviewSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetSANReviewScheduleForPersonService(
  @Autowired val sanGateway: SANGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun getReviewSchedules(hmppsId: String): Response<PlanReviewSchedules> {
    val response = getPersonService.getNomisNumber(hmppsId = hmppsId)
    val nomisNumber = response.data?.nomisNumber

    nomisNumber?.let {
      val schedulesResponse =  sanGateway.getReviewSchedules(it)
      val updatedSchedules = schedulesResponse.data.planReviewSchedules
        .map { schedule -> schedule.copy(nomisNumber = it) }

      return Response(
        PlanReviewSchedules(updatedSchedules),
        schedulesResponse.errors
      )
    }
    return Response(PlanReviewSchedules(listOf()), response.errors)
  }
}
