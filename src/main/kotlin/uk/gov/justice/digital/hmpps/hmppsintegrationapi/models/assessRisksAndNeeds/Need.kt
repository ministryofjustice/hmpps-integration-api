package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Need as IntegrationApiNeed

data class Need(
  val section: String? = null,
) {
  fun toNeed(): IntegrationApiNeed = IntegrationApiNeed(
    type = this.section,
  )
}
