package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.time.LocalDate

data class Sentence(
  val dataSource: UpstreamApi,
  val dateOfSentencing: LocalDate? = null,
  val description: String? = null,
  val isActive: Boolean? = null,
  val isCustodial: Boolean,
  val fineAmount: Number? = null,
  val length: SentenceLength = SentenceLength(),
)
