package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class OffenderSearchResponse(
  val prisonerOffenderSearch: Person?,
  val probationOffenderSearch: Person?,
)
