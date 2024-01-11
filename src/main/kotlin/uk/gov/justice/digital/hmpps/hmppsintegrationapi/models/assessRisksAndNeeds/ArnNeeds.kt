package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Needs as IntegrationApiNeeds

data class ArnNeeds(
  val assessedOn: LocalDateTime? = null,
  val identifiedNeeds: List<ArnNeed> = emptyList(),
  val notIdentifiedNeeds: List<ArnNeed> = emptyList(),
  val unansweredNeeds: List<ArnNeed> = emptyList(),
) {
  fun toNeeds(): IntegrationApiNeeds = IntegrationApiNeeds(
    assessedOn = this.assessedOn,
    identifiedNeeds = this.identifiedNeeds.mapNotNull { it.toNeed() },
    notIdentifiedNeeds = this.notIdentifiedNeeds.mapNotNull { it.toNeed() },
    unansweredNeeds = this.unansweredNeeds.mapNotNull { it.toNeed() },
  )
}
