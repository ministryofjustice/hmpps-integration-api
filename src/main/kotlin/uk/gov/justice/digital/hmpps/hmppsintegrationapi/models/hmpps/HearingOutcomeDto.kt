package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class HearingOutcomeDto(
  val id: Number? = null,
  val adjudicator: String? = null,
  val code: String? = null,
  val reason: String? = null,
  val details: String? = null,
  val plea: String? = null,
)
