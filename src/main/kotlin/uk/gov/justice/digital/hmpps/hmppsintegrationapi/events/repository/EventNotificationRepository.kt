package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository

import java.time.LocalDateTime

interface EventNotificationRepository {
  fun deleteEvents(dateTime: LocalDateTime): Int
}
