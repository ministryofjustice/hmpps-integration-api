package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

data class SentenceLength(
  val duration: Int? = null,
  val units: String? = null,
  val terms: List<SentenceTerm> = listOf(SentenceTerm()),
)
