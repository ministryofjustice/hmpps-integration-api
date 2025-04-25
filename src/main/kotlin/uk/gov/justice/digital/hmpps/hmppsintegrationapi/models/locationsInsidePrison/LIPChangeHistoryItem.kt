package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

data class LIPChangeHistoryItem(
  val transactionId: String?,
  val transactionType: String?,
  val attribute: String,
  val oldValues: List<String>?,
  val newValues: List<String>?,
  val amendedBy: String,
  val amendedDate: String,
)
