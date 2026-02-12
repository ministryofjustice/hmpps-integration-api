package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import java.time.LocalDateTime

interface EventNotificationRepository {
  fun deleteEvents(dateTime: LocalDateTime): Int

  fun getStuckEvents(minusMinutes: LocalDateTime)

  fun setProcessed(eventId: Any)

  fun setPending(eventId: Any)

  fun setProcessing(
    fiveMinutesAgo: LocalDateTime,
    claimId: String,
  )

  fun findAllProcessingEvents(claimId: String)

  fun save(makeEvent: EventNotification)

  fun deleteAll()

  fun findAll()
}
