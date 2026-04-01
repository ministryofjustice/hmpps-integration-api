package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities
import com.fasterxml.jackson.annotation.JsonIncludeProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped
import java.time.LocalDateTime

@JsonIncludeProperties("eventId", "hmppsId", "eventType", "prisonId", "url", "lastModifiedDateTime", "filters")
data class EventNotification(
  val eventId: Long? = null,
  val claimId: String? = null,
  val hmppsId: String? = null,
  val eventType: String,
  val prisonId: String? = null,
  val url: String,
  val status: String? = "PENDING",
  @JsonProperty("lastModifiedDateTime")
  val lastModifiedDatetime: LocalDateTime? = null,
  @JsonUnwrapped
  val filters: Filters? = null,
)

data class Filters(
  val supervisionStatus: String? = null,
)

enum class IntegrationEventStatus {
  PENDING,
  PROCESSING,
  PROCESSED,
}
