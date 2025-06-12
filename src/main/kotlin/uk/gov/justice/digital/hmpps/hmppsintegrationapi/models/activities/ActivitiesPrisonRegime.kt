package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonRegime
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
) {
  fun toPrisonRegime() =
    PrisonRegime(
      amStart = this.amStart,
      amFinish = this.amFinish,
      pmStart = this.pmStart,
      pmFinish = this.pmFinish,
      edStart = this.edStart,
      edFinish = this.edFinish,
      dayOfWeek = this.dayOfWeek,
    )
}
