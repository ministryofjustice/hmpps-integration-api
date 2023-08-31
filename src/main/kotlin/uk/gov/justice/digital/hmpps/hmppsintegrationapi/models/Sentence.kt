package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Length as IntegrationApiLength

data class Sentence(
  val dataSource: UpstreamApi,
  val dateOfSentencing: LocalDate? = null,
  val description: String? = null,
  val isActive: Boolean? = null,
  val isCustodial: Boolean,
  val fineAmount: Number? = null,
  val length: IntegrationApiLength = IntegrationApiLength(),
)
