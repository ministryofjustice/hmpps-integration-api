package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class CaseNoteAmendment(
  @Schema(description = "The ID of the Case Note in uuid format")
  @JsonProperty("id")
  val caseNoteAmendmentId: String? = null,
  @Schema(description = "Date and Time of Case Note creation", example = "2023-09-05T10:15:41")
  val creationDateTime: LocalDateTime? = null,
  @Schema(example = "Some Additional Text")
  val additionalNoteText: String? = null,
)
