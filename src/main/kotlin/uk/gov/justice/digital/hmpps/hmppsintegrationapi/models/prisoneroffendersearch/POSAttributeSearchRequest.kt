package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

data class POSAttributeSearchRequest(
  val joinType: String = "AND",
  val queries: List<POSAttributeSearchQuery>,
  val pagination: POSPaginationRequest = POSPaginationRequest(),
) {
  fun toMap(): Map<String, Any> =
    mapOf(
      "joinType" to this.joinType,
      "queries" to this.queries.map { it.toMap() },
      "pagination" to this.pagination,
    )
}

data class POSAttributeSearchQuery(
  val joinType: String = "AND",
  val matchers: List<POSAttributeSearchMatcher>,
) {
  fun toMap(): Map<String, Any> =
    mapOf(
      "joinType" to this.joinType,
      "matchers" to this.matchers.map { it.toMap() },
    )
}

data class POSAttributeSearchMatcher(
  val type: String = "String",
  val attribute: String,
  val condition: String,
  val searchTerm: String,
) {
  fun toMap(): Map<String, String> =
    mapOf(
      "type" to this.type,
      "attribute" to this.attribute,
      "condition" to this.condition,
      "searchTerm" to this.searchTerm,
    )
}
