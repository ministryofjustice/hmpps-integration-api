package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence as IntegrationApiSentence

data class Sentence(
  val date: String? = null,
)
