package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class CaseNote(
  @Schema(example = "1234")
  val caseNoteId: String? = null,
  @Schema(example = "A1234AA")
  val offenderIdentifier: String? = null,
  @Schema(example = "KA")
  val type: String? = null,
  @Schema(example = "Key Worker")
  val typeDescription: String? = null,
  @Schema(example = "KS")
  val subType: String? = null,
  @Schema(example = "Key Worker Session")
  val subTypeDescription: String? = null,
  @Schema(description = "Date and Time of Case Note creation", example = "2023-09-05T10:15:41")
  val creationDateTime: LocalDateTime? = null,
  @Schema(description = "Date and Time of when case note contact with offender was made", example = "2023-09-05T10:15:41")
  val occurrenceDateTime: LocalDateTime? = null,
  @Schema(example = "This is some text")
  val text: String? = null,
  @Schema(example = "MDI")
  val locationId: String? = null,
  val sensitive: Boolean = false,
  @Schema(description = "List of amendments to the case note")
  val amendments: List<CaseNoteAmendment> = emptyList(),
)
