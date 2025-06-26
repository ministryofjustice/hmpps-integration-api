package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.WaitingListApplication
import java.time.LocalDate
import java.time.LocalDateTime

data class ActivitiesWaitingListApplication(
  val id: Long,
  val activityId: Long,
  val scheduleId: Long,
  val allocationId: Long?,
  val prisonCode: String,
  val prisonerNumber: String,
  val bookingId: Long,
  val status: String,
  val statusUpdatedTime: LocalDateTime?,
  val requestedDate: LocalDate,
  val requestedBy: String,
  val comments: String?,
  val declinedReason: String?,
  val creationTime: LocalDateTime,
  val createdBy: String,
  val updatedTime: LocalDateTime?,
  val updatedBy: String?,
  val earliestReleaseDate: ActivitiesEarliestReleaseDate,
  val nonAssociations: Boolean?,
) {
  fun toWaitingListApplication(): WaitingListApplication =
    WaitingListApplication(
      id = id,
      activityId = activityId,
      scheduleId = scheduleId,
      allocationId = allocationId,
      prisonId = prisonCode,
      prisonerNumber = prisonerNumber,
      bookingId = bookingId,
      status = status,
      statusUpdatedTime = statusUpdatedTime,
      requestedDate = requestedDate,
      comments = comments,
      declinedReason = declinedReason,
      creationTime = creationTime,
      updatedTime = updatedTime,
      earliestReleaseDate = earliestReleaseDate.toEarliestReleaseDate(),
      nonAssociations = nonAssociations,
    )
}
