package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Need

data class ArnNeed(
  val section: String? = null,
  val riskOfHarm: Boolean? = null,
  val riskOfReoffending: Boolean? = null,
) {
  fun toNeed(): Need =
    Need(
      type = this.section,
      riskOfHarm = this.riskOfHarm,
      riskOfReoffending = this.riskOfReoffending,
    )
}
