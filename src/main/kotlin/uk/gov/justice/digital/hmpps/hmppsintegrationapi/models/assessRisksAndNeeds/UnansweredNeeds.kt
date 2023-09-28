package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UnansweredNeeds as IntegrationApiUnansweredNeeds

data class UnansweredNeeds(
  val section: String? = null,
) {
  fun toUnansweredNeeds(): IntegrationApiUnansweredNeeds = IntegrationApiUnansweredNeeds(
    type = this.section,
  )
}
