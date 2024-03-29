package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class SentenceLength(
  val duration: Int? = null,
  val units: String? = null,
  val terms: List<SentenceTerm> = emptyList(),
)
