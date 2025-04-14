package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNoteAmendment

data class OCNCaseNote(
  val content: List<NomisCaseNote> = listOf(),
  @JsonProperty("metadata")
  val page: OCNPagination,
) {
  fun toCaseNotes(): List<CaseNote> =
    this.content.map {
      val amendments =
        it.amendments
          .stream()
          .map { amendment ->
            CaseNoteAmendment(amendment?.caseNoteAmendmentId, amendment?.creationDateTime, amendment?.additionalNoteText)
          }.toList()
      CaseNote(
        it.caseNoteId,
        it.offenderIdentifier,
        it.type,
        it.typeDescription,
        it.subType,
        it.subTypeDescription,
        it.creationDateTime,
        it.occurrenceDateTime,
        it.text,
        it.locationId,
        it.sensitive,
        amendments,
      )
    }
}

data class OCNPagination(
  @Schema(description = "Current Page")
  val page: Int,
  @Schema(description = "Total elements")
  val totalElements: Int,
  @Schema(description = "Total pages")
  val size: Int,
)
