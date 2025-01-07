package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PLPGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReviewSchedules

@Service
class GetReviewScheduleForPersonService(
  @Autowired val plpGateway: PLPGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<ReviewSchedules> {
    // Step 1: Get the NOMIS number for the given HMPPS ID
    val nomisNumberResponse = getPersonService.getNomisNumber(hmppsId)
    val nomisNumber =
      nomisNumberResponse.data?.nomisNumber
        ?: return Response(ReviewSchedules(emptyList()), nomisNumberResponse.errors)

    // Step 2: Fetch completed reviews
    val plpReviewsResponse = plpGateway.getReviews(nomisNumber)
    if (plpReviewsResponse.errors.isNotEmpty()) {
      return Response(ReviewSchedules(emptyList()), plpReviewsResponse.errors)
    }
    val completedReviews = plpReviewsResponse.data.completedReviews
    val mappedReviews = completedReviews.associateBy { it.reviewScheduleReference }

    // Step 3: Fetch review schedules
    val reviewSchedulesResponse = plpGateway.getReviewSchedules(nomisNumber)
    if (reviewSchedulesResponse.errors.isNotEmpty()) {
      return Response(ReviewSchedules(emptyList()), reviewSchedulesResponse.errors)
    }

    // Step 4: Update review schedules with completed reviews
    val updatedReviewSchedules =
      reviewSchedulesResponse.data.reviewSchedules.map { reviewSchedule ->
        val completed = mappedReviews[reviewSchedule.reference]
        if (reviewSchedule.status == "COMPLETED" && completed != null) {
          reviewSchedule.copy(
            reviewCompletedBy = completed.conductedBy ?: completed.updatedByDisplayName,
            reviewCompletedByRole = completed.conductedByRole ?: "CIAG",
            reviewCompletedAt = completed.updatedAt,
            preRelease = completed.preRelease,
          )
        } else {
          reviewSchedule
        }
      }
    // Step 5: Return the updated review schedules
    return Response(
      ReviewSchedules(updatedReviewSchedules),
      emptyList(),
    )
  }
}
