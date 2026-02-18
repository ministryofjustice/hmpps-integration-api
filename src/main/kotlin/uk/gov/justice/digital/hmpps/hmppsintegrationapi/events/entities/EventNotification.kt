package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities
import com.fasterxml.jackson.annotation.JsonIncludeProperties
import java.time.LocalDateTime

@JsonIncludeProperties("eventId", "hmppsId", "eventType", "prisonId", "url", "lastModifiedDateTime")
data class EventNotification(
  val eventId: Long? = null,
  val claimId: String? = null,
  val hmppsId: String? = null,
  val eventType: String,
  val prisonId: String? = null,
  val url: String,
  val status: String? = null,
  val lastModifiedDatetime: LocalDateTime? = null,
)

enum class IntegrationEventStatus {
  PENDING,
  PROCESSING,
  PROCESSED,
}
