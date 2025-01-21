package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class ExpressionOfInterestMessage(
  val jobId: String,
  val verifiedHmppsId: String?,
  val eventType: String = "ExpressionOfInterest",
)
