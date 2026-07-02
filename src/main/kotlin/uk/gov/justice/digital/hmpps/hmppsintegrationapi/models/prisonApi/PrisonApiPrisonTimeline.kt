package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

data class PrisonApiPrisonTimeline(
  var prisonPeriod: List<PrisonPeriodEntry> = emptyList(),
)

data class PrisonPeriodEntry(
  var entryDate: String? = null,
  var releaseDate: String? = null,
  var movementDates: List<MovementDatesEntry> = emptyList(),
  var transfers: List<TransfersEntry> = emptyList(),
  var prisons: List<String> = emptyList(),
)

data class MovementDatesEntry(
  var reasonInToPrison: String? = null,
  var dateInToPrison: String? = null,
  var inwardType: String? = null,
  var reasonOutOfPrison: String? = null,
  var dateOutOfPrison: String? = null,
  var outwardType: String? = null,
  var admittedIntoPrisonId: String? = null,
  var releaseFromPrisonId: String? = null,
)

data class TransfersEntry(
  var dateOutOfPrison: String? = null,
  var dateInToPrison: String? = null,
  var transferReason: String? = null,
  var fromPrisonId: String? = null,
  var toPrisonId: String? = null,
)
