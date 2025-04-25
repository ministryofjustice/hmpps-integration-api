package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

data class LIPTransactionHistoryItem(
  val transactionId: String,
  val transactionType: String,
  val prisonId: String,
  val transactionDetail: String,
  val transactionInvokedBy: String,
  val txStartTime: String,
  val transactionDetails: List<LIPTransactionDetails>
)

data class LIPTransactionDetails(
  val locationId: String,
  val locationKey: String,
  val attributeCode: String,
  val amendedBy: String,
  val amendedDate: String,
  val oldValues: List<String>?,
  val newValues: List<String>?,
)
