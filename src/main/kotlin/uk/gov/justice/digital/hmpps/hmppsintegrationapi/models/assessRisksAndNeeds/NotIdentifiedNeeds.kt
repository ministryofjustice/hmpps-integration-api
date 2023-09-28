package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.NotIdentifiedNeeds as IntegrationApiNotIdentifiedNeeds

data class NotIdentifiedNeeds(
  val section: String? = null,
) {
  fun toNotIdentifiedNeeds(): IntegrationApiNotIdentifiedNeeds = IntegrationApiNotIdentifiedNeeds(
    type = this.section,
  )
}
