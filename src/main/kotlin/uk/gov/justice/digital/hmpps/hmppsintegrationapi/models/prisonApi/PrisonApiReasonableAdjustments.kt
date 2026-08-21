package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

data class PrisonApiReasonableAdjustments(
  val reasonableAdjustments: List<PrisonApiReasonableAdjustment> = emptyList(),
)
