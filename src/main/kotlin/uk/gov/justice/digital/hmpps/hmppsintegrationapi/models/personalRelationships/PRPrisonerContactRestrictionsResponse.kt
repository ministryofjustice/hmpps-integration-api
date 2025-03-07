package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

data class PRPrisonerContactRestrictionsResponse(
  var prisonerContactRestrictions: MutableList<PrisonerContactRestriction>? = mutableListOf(),
  var contactGlobalRestrictions: List<ContactGlobalRestriction>? = listOf(),
)
