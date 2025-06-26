package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesWaitingListSearchRequest
import java.time.LocalDate

data class WaitingListSearchRequest(
  val applicationDateFrom: LocalDate? = null,
  val applicationDateTo: LocalDate? = null,
  val activityId: Long? = null,
  val prisonerNumbers: List<String>? = null,
  val status: List<String>? = null,
) {
  fun toActivitiesWaitingListSearchRequest(): ActivitiesWaitingListSearchRequest =
    ActivitiesWaitingListSearchRequest(
      applicationDateFrom = applicationDateFrom,
      applicationDateTo = applicationDateTo,
      activityId = activityId,
      prisonerNumbers = prisonerNumbers,
      status = status,
    )
}
