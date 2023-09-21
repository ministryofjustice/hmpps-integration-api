package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Risks as IntegrationApiRisks

data class Risks(
  val assessedOn: LocalDateTime? = null,
) {
  fun toRisks(): IntegrationApiRisks = IntegrationApiRisks(
    assessedOn = this.assessedOn,
  )
}
