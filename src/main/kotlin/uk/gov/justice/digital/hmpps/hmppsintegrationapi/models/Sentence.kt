package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Term as IntegrationApiTerm
data class Sentence(
  val dateOfSentencing: LocalDate? = null,
  val isActive: Boolean? = null,
  val terms: List<IntegrationApiTerm> = listOf(IntegrationApiTerm()),
)
