package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities
import java.time.LocalDateTime

data class EventNotification(
  val eventId: Long? = null,
  val claimId: String? = null,
  val hmppsId: String? = null,
  val eventType: String,
  val prisonId: String? = null,
  val url: String,
  val status: String,
  val lastModifiedDateTime: LocalDateTime,
)

enum class IntegrationEventStatus {
  PENDING,
  PROCESSING,
  PROCESSED,
}
