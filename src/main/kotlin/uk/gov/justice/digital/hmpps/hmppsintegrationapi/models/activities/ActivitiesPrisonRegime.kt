package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import java.time.DayOfWeek

data class ActivitiesPrisonRegime(
  val id: Long,
  val prisonCode: String,
  val amStart: String,
  val amFinish: String,
  val pmStart: String,
  val pmFinish: String,
  val edStart: String,
  val edFinish: String,
  val dayOfWeek: DayOfWeek,
)
