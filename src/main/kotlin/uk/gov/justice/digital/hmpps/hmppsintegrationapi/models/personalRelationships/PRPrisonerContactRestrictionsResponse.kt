package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

data class PRPrisonerContactRestrictionsResponse(
  var prisonerContactRestrictions: MutableList<PRPrisonerContactRestriction>? = mutableListOf(),
  var contactGlobalRestrictions: List<PRContactGlobalRestriction>? = listOf(),
)
