package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class VisitQueueEvent(
  val eventType: VisitQueueEventType,
  val payload: String,
  val who: String,
) {
  val `when`: String = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()).format(ZonedDateTime.now())
  val service = "hmpps-integration-api"
}

enum class VisitQueueEventType {
  CREATE,
  UPDATE,
  DELETE,
}
