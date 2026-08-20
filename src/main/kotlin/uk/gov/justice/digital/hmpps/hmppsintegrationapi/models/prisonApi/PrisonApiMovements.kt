package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

import java.time.LocalDateTime
import java.time.format.DateTimeParseException

data class PrisonApiMovements(
  val movements: List<MovementItem> = emptyList(),
) {
  fun toResponse(): PrisonApiMovementsResponse {
    val latestTRN =
      movements
        .filter { it.movementType == "TRN" }
        .maxWithOrNull(compareBy(nullsFirst()) { it.parsedMovementDateTime() })

    val latestCRT =
      movements
        .filter { it.movementType == "CRT" }
        .maxWithOrNull(compareBy(nullsFirst()) { it.parsedMovementDateTime() })

    val latestOUT =
      movements
        .filter { it.directionCode == "OUT" }
        .maxWithOrNull(compareBy(nullsFirst()) { it.parsedMovementDateTime() })

    return PrisonApiMovementsResponse(
      transferReason = latestTRN?.movementReason,
      movementCode = latestTRN?.movementReasonCode,
      toAgencyDescription = latestTRN?.toAgencyDescription,
      receivedFromDescription = latestTRN?.fromAgencyDescription,
      establishmentName = latestTRN?.fromAgencyDescription,
      movementDateTime = latestTRN?.parsedMovementDateTime()?.toString(),
      courtName = latestCRT?.toAgencyDescription,
      dateOfFirstMovement = latestOUT?.parsedMovementDateTime()?.toString(),
    )
  }
}

data class MovementItem(
  val fromAgencyDescription: String? = null,
  val toAgencyDescription: String? = null,
  val movementType: String? = null,
  val directionCode: String? = null,
  val movementDate: String? = null,
  val movementTime: String? = null,
  val movementReason: String? = null,
  val movementReasonCode: String? = null,
) {
  fun parsedMovementDateTime(): LocalDateTime? {
    if (movementDate.isNullOrBlank() || movementTime.isNullOrBlank()) {
      return null
    }

    return try {
      LocalDateTime.parse("${movementDate}T$movementTime")
    } catch (e: DateTimeParseException) {
      null
    }
  }
}
