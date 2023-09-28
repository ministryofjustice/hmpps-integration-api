package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Needs as IntegrationApiNeeds

data class Needs(
  val assessedOn: LocalDateTime? = null,
  val unansweredNeeds: UnansweredNeeds = UnansweredNeeds(),
) {
  fun toNeeds(): IntegrationApiNeeds = IntegrationApiNeeds(
    assessedOn = this.assessedOn,
    unansweredNeeds = this.unansweredNeeds.toUnansweredNeeds(),
  )
}
