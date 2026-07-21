package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ukDateTimeToInstant

data class PrisonApiPrisonTimeline(
  var prisonPeriod: List<PrisonPeriodEntry> = emptyList(),
)

data class PrisonPeriodEntry(
  @Schema(example = "2023-12-08T15:50:37Z")
  var entryDate: String? = null,
  @Schema(example = "2023-12-08T16:21:24Z")
  var releaseDate: String? = null,
  var movementDates: List<MovementDatesEntry> = emptyList(),
  var transfers: List<TransfersEntry> = emptyList(),
  @Schema(example = """["BMI", "AYI"]""")
  var prisons: List<String> = emptyList(),
)

data class MovementDatesEntry(
  @Schema(example = "Imprisonment Without Option")
  var reasonInToPrison: String? = null,
  @Schema(example = "2023-12-08T15:50:37Z")
  var dateInToPrison: String? = null,
  @Schema(example = "ADM")
  var inwardType: String? = null,
  @Schema(example = "Conditional Release (CJA91) -SH Term>1YR")
  var reasonOutOfPrison: String? = null,
  @Schema(example = "2023-12-08T16:21:24Z")
  var dateOutOfPrison: String? = null,
  @Schema(example = "REL")
  var outwardType: String? = null,
  @Schema(example = "BMI")
  var admittedIntoPrisonId: String? = null,
  @Schema(example = "AYI")
  var releaseFromPrisonId: String? = null,
)

data class TransfersEntry(
  @Schema(example = "2023-12-08T15:51:09Z")
  var dateOutOfPrison: String? = null,
  @Schema(example = "2023-12-08T16:19:45Z")
  var dateInToPrison: String? = null,
  @Schema(example = "Appeals")
  var transferReason: String? = null,
  @Schema(example = "BMI")
  var fromPrisonId: String? = null,
  @Schema(example = "AYI")
  var toPrisonId: String? = null,
)

fun PrisonApiPrisonTimeline.convertDatesToInstant(): PrisonApiPrisonTimeline =
  this.apply {
    prisonPeriod.forEach { period ->
      // Convert PrisonPeriodEntry dates
      period.entryDate = ukDateTimeToInstant(period.entryDate)
      period.releaseDate = ukDateTimeToInstant(period.releaseDate)

      // Convert MovementDatesEntry dates
      period.movementDates.forEach { movement ->
        movement.dateInToPrison = ukDateTimeToInstant(movement.dateInToPrison)
        movement.dateOutOfPrison = ukDateTimeToInstant(movement.dateOutOfPrison)
      }

      // Convert TransfersEntry dates
      period.transfers.forEach { transfer ->
        transfer.dateInToPrison = ukDateTimeToInstant(transfer.dateInToPrison)
        transfer.dateOutOfPrison = ukDateTimeToInstant(transfer.dateOutOfPrison)
      }
    }
  }
