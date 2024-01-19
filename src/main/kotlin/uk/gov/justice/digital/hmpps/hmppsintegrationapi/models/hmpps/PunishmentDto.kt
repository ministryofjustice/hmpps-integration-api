package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PunishmentDto(
  val id: Number? = null,
  val type: String? = null,
  val privilegeType: String? = null,
  val otherPrivilege: String? = null,
  val stoppagePercentage: Number? = null,
  val activatedBy: String? = null,
  val activatedFrom: String? = null,
  val schedule: PunishmentScheduleDto? = null,
  val consecutiveChargeNumber: String? = null,
  val consecutiveReportAvailable: Boolean? = null,
  val damagesOwedAmount: Number? = null,
  val canRemove: Boolean? = null,
)
