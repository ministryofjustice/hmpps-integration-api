package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class Sentence(
  val dateOfSentencing: LocalDate? = null,
)
