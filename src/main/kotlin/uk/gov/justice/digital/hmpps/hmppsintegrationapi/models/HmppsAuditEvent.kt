package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class HmppsAuditEvent(
  val what: String,
  val details: String,
  val who: String,
) {
  val `when`: String = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()).format(ZonedDateTime.now())
  val service = "hmpps-integration-api"
}