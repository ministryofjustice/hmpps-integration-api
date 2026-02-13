package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import java.time.LocalDateTime

interface EventNotificationRepository {
  fun deleteEvents(dateTime: LocalDateTime): Int

  fun getStuckEvents(minusMinutes: LocalDateTime): List<EventNotification>

  fun setProcessed(eventId: Any): Int

  fun setPending(eventId: Any): Int

  fun setProcessing(
    fiveMinutesAgo: LocalDateTime,
    claimId: String,
  ): Int

  fun findAllProcessingEvents(claimId: String): List<EventNotification>

  fun save(makeEvent: EventNotification): Int

  fun deleteAll(): Int

  fun findAll(): List<EventNotification>

  fun findAllWithLastModifiedDateTimeBefore(dateTimeBefore: LocalDateTime): List<EventNotification>

  fun deleteById(id: Int): Int
}
