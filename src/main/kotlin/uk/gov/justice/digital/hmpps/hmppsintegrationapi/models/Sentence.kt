package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class Sentence(
  val startDate: LocalDate? = null,
  val length: SentenceLength = SentenceLength(),
  val fineAmount: Double? = null,
  val isLifeSentence: Boolean?,
)
