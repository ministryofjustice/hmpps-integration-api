package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

data class PrisonApiScheduledEvents(
  val eventSubType: String? = null,
  val startTime: String? = null,
  val outcomeComment: String? = null,
) {
  fun toMovementDiary(): MovementDiary =
    MovementDiary(
      diaryDateTime = this.startTime,
      diaryReasonCode = this.eventSubType,
      diaryComments = this.outcomeComment,
    )
}
