package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cemo

import java.util.UUID

data class CemoOrderCaseSearchResult(
  val id: UUID,
  val versions: List<Map<String, Any?>>,
)
