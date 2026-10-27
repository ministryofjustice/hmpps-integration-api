package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ukDateTimeToInstant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class PrisonApiEvents(
  val events: List<PrisonApiEvent> = emptyList(),
) {
  fun convertTimestamps(): PrisonApiEvents = PrisonApiEvents(this.events.map { it.convertTimestamp() })
}

data class PrisonApiEvent(
  @Schema(example = "IEP_CHANGED")
  val type: String? = null,
  @Schema(example = "21")
  val id: Int? = null,
  @Schema(example = "A1417AE")
  val noms_id: String? = null,
  @Schema(example = "BMI")
  val prison_id: String? = null,
  @Schema(example = "2016-10-21T14:55:06.284Z")
  val timestamp: String? = null,
  @Schema(example = "string")
  val eventData: String? = null,
) {
  fun convertTimestamp(): PrisonApiEvent =
    PrisonApiEvent(
      type = this.type,
      id = this.id,
      noms_id = this.noms_id,
      prison_id = this.prison_id,
      timestamp = parseTimestamp(this.timestamp),
      eventData = this.eventData,
    )
}

private fun parseTimestamp(timestamp: String?): String? {
  if (timestamp == null) {
    return null
  }
  val timestampToDateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  return ukDateTimeToInstant(timestampToDateTime)
}
