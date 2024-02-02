package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

data class NomisSentenceSummary(
  val latestPrisonTerm: NomisPrisonTerm = NomisPrisonTerm(),
)
