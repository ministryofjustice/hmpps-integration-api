package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class OffenderSearchResponse(
  val probationOffenderSearch: Person?,
  val prisonerOffenderSearch: Person?,
)
