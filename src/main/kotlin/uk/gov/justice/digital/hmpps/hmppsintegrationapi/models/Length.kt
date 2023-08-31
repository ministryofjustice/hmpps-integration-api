package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

data class Length(
  val duration: Int? = null,
  val units: String? = null,
  val terms: List<Term> = listOf(Term()),
)
