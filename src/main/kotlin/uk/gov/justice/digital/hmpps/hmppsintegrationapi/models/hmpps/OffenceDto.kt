package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class OffenceDto(
  val offenceCode: Number? = null,
  val offenceRule: OffenceRuleDto? = null,
  val victimPrisonersNumber: String? = null,
  val victimStaffUsername: String? = null,
  val victimOtherPersonsName: String? = null,
)
