package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PLPGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReviewSchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReviewSchedules
import java.time.LocalDate

@Service
class GetReviewScheduleForPersonService(
  @Autowired val plpGateway: PLPGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<ReviewSchedules> {
    val response = getPersonService.getNomisNumber(hmppsId = hmppsId)

    response.data?.nomisNumber?.let {
      return plpGateway.getReviewSchedules(it)
    }
    return Response(ReviewSchedules(listOf()), response.errors)
  }
}
