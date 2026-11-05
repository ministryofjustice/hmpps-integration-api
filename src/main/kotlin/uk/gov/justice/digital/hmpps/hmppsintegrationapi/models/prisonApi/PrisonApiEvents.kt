package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ukDateTimeToInstant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class PrisonApiEvents(
  val events: List<PrisonApiEvent> = emptyList(),
) {
  fun convertTimestamps(): PrisonApiEvents = PrisonApiEvents(this.events.map { it.convertTimestamp() })
}

@JsonInclude(JsonInclude.Include.NON_NULL)
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
  /*
  The 1 of the following properties can be returned by the upstream
  The upstream takes these values directly from the NOMIS database and are already in JSON Format
   */
  val alert: Map<String, Any>? = null,
  val discharge: Map<String, Any>? = null,
  val iep_changed: Map<String, Any>? = null,
  val internal_location_changed: Map<String, Any>? = null,
  val nomis_id_changed: Map<String, Any>? = null,
  val personal_details_changed: Map<String, Any>? = null,
  val personal_officer_changed: Map<String, Any>? = null,
  val reception: Map<String, Any>? = null,
  val sentence_information_changed: Map<String, Any>? = null,
  val balance_update: Map<String, Any>? = null,
) {
  fun convertTimestamp(): PrisonApiEvent =
    PrisonApiEvent(
      type = this.type,
      id = this.id,
      noms_id = this.noms_id,
      prison_id = this.prison_id,
      timestamp = parseTimestamp(this.timestamp),
      alert = this.alert,
      discharge = this.discharge,
      iep_changed = this.iep_changed,
      internal_location_changed = this.internal_location_changed,
      nomis_id_changed = this.nomis_id_changed,
      personal_details_changed = this.personal_details_changed,
      personal_officer_changed = this.personal_officer_changed,
      reception = this.reception,
      sentence_information_changed = this.sentence_information_changed,
      balance_update = this.balance_update,
    )
}

private fun parseTimestamp(timestamp: String?): String? {
  if (timestamp == null) {
    return null
  }
  val timestampToDateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  return ukDateTimeToInstant(timestampToDateTime)
}
