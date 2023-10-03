package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Needs as IntegrationApiNeeds

data class Needs(
  val assessedOn: LocalDateTime? = null,
  val identifiedNeeds: List<Need> = emptyList(),
  val notIdentifiedNeeds: List<Need> = emptyList(),
  val unansweredNeeds: List<Need> = emptyList(),
) {
  fun toNeeds(): IntegrationApiNeeds = IntegrationApiNeeds(
    assessedOn = this.assessedOn,
    identifiedNeeds = this.identifiedNeeds.mapNotNull { it.toNeed() },
    notIdentifiedNeeds = this.notIdentifiedNeeds.mapNotNull { it.toNeed() },
    unansweredNeeds = this.unansweredNeeds.mapNotNull { it.toNeed() },
  )
}
