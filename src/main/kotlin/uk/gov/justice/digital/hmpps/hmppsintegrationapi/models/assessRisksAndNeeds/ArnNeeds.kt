package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Needs
import java.time.LocalDateTime

data class ArnNeeds(
  val assessedOn: LocalDateTime? = null,
  val needs: List<ArnNeed> = emptyList(),
) {
  fun toNeeds(): Needs =
    Needs(
      assessedOn = this.assessedOn,
      needs = this.needs.map { it.toNeed() },
    )
}
