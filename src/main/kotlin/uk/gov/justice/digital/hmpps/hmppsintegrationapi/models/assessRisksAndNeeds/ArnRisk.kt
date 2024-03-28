package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risk

data class ArnRisk(
  val risk: String? = null,
  val previous: String? = null,
  val previousConcernsText: String? = null,
  val current: String? = null,
  val currentConcernsText: String? = null,
) {
  fun toRisk(): Risk =
    Risk(
      risk = this.risk,
      previous = this.previous,
      previousConcernsText = this.previousConcernsText,
      current = this.current,
      currentConcernsText = this.currentConcernsText,
    )
}
