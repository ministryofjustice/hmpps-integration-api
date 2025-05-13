package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

data class POSAttributeSearchRequest(
  val joinType: String = "AND",
  val queries: List<POSAttributeSearchQuery>,
  val pagination: POSPaginationRequest = POSPaginationRequest(),
)

data class POSAttributeSearchQuery(
  val joinType: String = "AND",
  val matchers: List<POSAttributeSearchMatcher>,
)

data class POSAttributeSearchMatcher(
  val type: String = "String",
  val attribute: String,
  val condition: String,
  val searchTerm: String,
)
