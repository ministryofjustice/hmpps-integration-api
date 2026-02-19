package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.StuckEvents
import java.time.LocalDateTime

interface EventNotificationRepository {
  // Get
  fun getStuckEvents(minusMinutes: LocalDateTime): List<StuckEvents>

  fun findAllWithLastModifiedDateTimeBefore(dateTimeBefore: LocalDateTime): List<EventNotification>

  fun findAllProcessingEvents(claimId: String): List<EventNotification>

  // Update
  fun setProcessed(eventId: Any): Int

  fun setPending(eventId: Any): Int

  fun setProcessing(
    fiveMinutesAgo: LocalDateTime,
    claimId: String,
  ): Int

  // Delete
  fun deleteEvents(dateTime: LocalDateTime): Int
}
