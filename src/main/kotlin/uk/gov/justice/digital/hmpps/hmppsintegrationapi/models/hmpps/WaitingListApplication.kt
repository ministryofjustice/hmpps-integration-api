package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.time.LocalDate
import java.time.LocalDateTime

data class WaitingListApplication(
  val id: Long,
  val activityId: Long,
  val scheduleId: Long,
  val allocationId: Long?,
  val prisonId: String,
  val prisonerNumber: String,
  val bookingId: Long,
  val status: String,
  val statusUpdatedTime: LocalDateTime?,
  val requestedDate: LocalDate,
  val comments: String?,
  val declinedReason: String?,
  val creationTime: LocalDateTime,
  val updatedTime: LocalDateTime?,
  val earliestReleaseDate: EarliestReleaseDate,
  val nonAssociations: Boolean?,
)
