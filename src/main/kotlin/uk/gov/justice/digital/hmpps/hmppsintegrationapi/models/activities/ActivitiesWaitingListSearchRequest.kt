package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import java.time.LocalDate

data class ActivitiesWaitingListSearchRequest(
  val applicationDateFrom: LocalDate? = null,
  val applicationDateTo: LocalDate? = null,
  val activityId: Long? = null,
  val prisonerNumbers: List<String>? = null,
  val status: List<String>? = null,
) {
  fun toApiConformingMap(): Map<String, Any?> =
    mapOf(
      "applicationDateFrom" to applicationDateFrom?.toString(),
      "applicationDateTo" to applicationDateTo?.toString(),
      "activityId" to activityId,
      "prisonerNumbers" to prisonerNumbers,
      "status" to status,
    ).filterValues { it != null }
}
