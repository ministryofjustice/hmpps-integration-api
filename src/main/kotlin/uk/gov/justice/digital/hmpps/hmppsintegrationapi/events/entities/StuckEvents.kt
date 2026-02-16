package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities

import java.time.LocalDateTime

data class StuckEvents(
  val eventCount: Int,
  val status: String?,
  val earliestDatetime: LocalDateTime,
)
