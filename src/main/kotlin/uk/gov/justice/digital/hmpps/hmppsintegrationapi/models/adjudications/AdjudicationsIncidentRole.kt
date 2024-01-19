package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.adjudications

data class AdjudicationsIncidentRole(
  val roleCode: String? = null,
  val offenceRule: AdjudicationsOffenceRuleDetails? = null,
  val dateTimeOfDiscovery: String? = null,
  val handoverDeadline: String? = null,
)
