package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Need

data class ArnNeed(
  val section: String? = null,
) {
  fun toNeed(): Need = Need(
    type = this.section,
  )
}
