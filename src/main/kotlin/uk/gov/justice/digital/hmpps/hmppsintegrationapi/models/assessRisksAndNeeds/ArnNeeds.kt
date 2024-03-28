package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Needs
import java.time.LocalDateTime

data class ArnNeeds(
  val assessedOn: LocalDateTime? = null,
  val identifiedNeeds: List<ArnNeed> = emptyList(),
  val notIdentifiedNeeds: List<ArnNeed> = emptyList(),
  val unansweredNeeds: List<ArnNeed> = emptyList(),
) {
  fun toNeeds(): Needs =
    Needs(
      assessedOn = this.assessedOn,
      identifiedNeeds = this.identifiedNeeds.map { it.toNeed() },
      notIdentifiedNeeds = this.notIdentifiedNeeds.map { it.toNeed() },
      unansweredNeeds = this.unansweredNeeds.map { it.toNeed() },
    )
}
