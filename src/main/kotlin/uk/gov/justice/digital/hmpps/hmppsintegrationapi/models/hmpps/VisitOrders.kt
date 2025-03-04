package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class VisitOrders(
  @Schema(description = "The prisoners remaining visit orders", example = "123456")
  val remainingVisitOrders: Long,
  @Schema(description = "The prisoners remaining privilege visit orders", example = "123456")
  val remainingPrivilegeVisitOrders: Long,
)
