package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PLPGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReviewSchedule
import java.time.LocalDate

@Service
class GetReviewScheduleForPersonService(
  @Autowired val plpGateway: PLPGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<ReviewSchedule> {
    val response = getPersonService.getNomisNumber(hmppsId = hmppsId)

    response.data?.nomisNumber?.let {
      return Response(
        ReviewSchedule(
          deadlineDate = LocalDate.now().plusMonths(1),
          nomisNumber = it,
          description = "This is a hardcoded response.",
        ),
      )
//      return plpGateway.getReviewSchedule(it) // <-- this will be call the PLP service once the downstream code is written
    }
    return Response(ReviewSchedule(), response.errors)
  }
}
