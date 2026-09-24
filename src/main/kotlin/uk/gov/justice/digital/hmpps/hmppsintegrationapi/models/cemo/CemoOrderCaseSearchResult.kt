package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cemo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class CemoOrderCaseSearchResult(
  val id: UUID,
  val versions: List<CemoOrderVersion>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CemoOrderVersion(
  val status: CemoOrderStatus,
)

enum class CemoOrderStatus {
  IN_PROGRESS,
  ERROR,
  SUBMITTED,
}
