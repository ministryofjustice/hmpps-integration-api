package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDateTime

data class Needs(
  val assessedOn: LocalDateTime? = null,
  val identifiedNeeds: List<Need> = listOf(Need()),
  val notIdentifiedNeeds: List<Need> = listOf(Need()),
  val unansweredNeeds: List<Need> = listOf(Need()),
)
