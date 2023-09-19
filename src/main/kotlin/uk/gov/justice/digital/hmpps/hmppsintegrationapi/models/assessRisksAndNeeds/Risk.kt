package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Risk as IntegrationApiRisk

data class Risk(
  val assessedOn: LocalDateTime? = null,
) {
  fun toRisk(): IntegrationApiRisk = IntegrationApiRisk(
    assessedOn = this.assessedOn,
  )
}
