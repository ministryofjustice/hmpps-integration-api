package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class OffenceDto(
  val offenceCode: Number? = null,
  val offenceRule: OffenceRuleDto? = null,
)
