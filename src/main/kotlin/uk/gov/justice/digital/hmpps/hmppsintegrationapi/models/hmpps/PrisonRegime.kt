package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.time.DayOfWeek

data class PrisonRegime(
  val amStart: String,
  val amFinish: String,
  val pmStart: String,
  val pmFinish: String,
  val edStart: String,
  val edFinish: String,
  val dayOfWeek: DayOfWeek,
)
