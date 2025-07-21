package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.SANGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanReviewSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetSANReviewScheduleForPersonService(
  @Autowired val sanGateway: SANGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun getReviewSchedules(hmppsId: String): Response<PlanReviewSchedules> {
    val response = getPersonService.getNomisNumber(hmppsId = hmppsId)

    response.data?.nomisNumber?.let {
      return sanGateway.getReviewSchedules(it)
    }
    return Response(PlanReviewSchedules(listOf()), response.errors)
  }
}
