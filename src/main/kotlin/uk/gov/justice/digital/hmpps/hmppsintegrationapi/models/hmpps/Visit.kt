package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class Visit(
  @Schema(description = "Application Reference", example = "dfs-wjs-eqr")
  val applicationReference: String?,
  @Schema(description = "Visit Reference", example = "v9-d7-ed-7u")
  val reference: String?,
  @Schema(description = "Prisoner Id", example = "AF34567G")
  val prisonerId: String,
  @Schema(description = "Prison Id", example = "MDI")
  val prisonId: String,
  @Schema(description = "Prison Name", example = "Moorland (HMP & YOI)")
  val prisonName: String?,
  @Schema(description = "Session Template Reference", example = "v9d.7ed.7u")
  val sessionTemplateReference: String?,
  @Schema(description = "Location of Visit ", example = "Visits Main Hall")
  val visitRoom: String,
  @Schema(description = "Type of Visit", example = "SOCIAL")
  val visitType: String,
  @Schema(description = "Visit Status", example = "BOOKED")
  val visitStatus: String,
  @Schema(description = "Outcome Status of Visit", example = "ADMINISTRATIVE_CANCELLATION")
  val outcomeStatus: String?,
  @Schema(description = "Visit Restriction", example = "OPEN")
  val visitRestriction: String,
  @Schema(description = "The date and time of the visit")
  val startTimestamp: String,
  @Schema(description = "The finishing date and time of the visit")
  val endTimestamp: String,
  @Schema(description = "Visit Notes")
  val visitNotes: List<VisitNotes>,
  @Schema(description = "Contact associated with the visit")
  val visitContact: VisitContact?,
  @Schema(description = "List of visitors associated with the visit")
  val visitors: List<Visitors>,
  @Schema(description = "Additional support associated with the visit")
  val visitorSupport: VisitorSupport?,
  @Schema(description = "External system details associated with the visit")
  val visitExternalSystemDetails: VisitExternalSystemDetails?,
  @Schema(description = "The visit created date and time")
  val createdTimestamp: String,
  @Schema(description = "The visit modified date and time")
  val modifiedTimestamp: String,
  @Schema(description = "Date the visit was first booked or migrated")
  val firstBookedDateTime: String,
)

data class VisitNotes(
  @Schema(description = "Note type", allowableValues = ["VISITOR_CONCERN", "VISIT_OUTCOMES", "VISIT_COMMENT", "STATUS_CHANGED_REASON"], example = "VISITOR_CONCERN", required = true)
  @field:NotBlank
  val type: String,
  @Schema(description = "Note Text", example = "Visitor is concerned that his mother in-law is coming!", required = true)
  @field:NotBlank
  val text: String,
)

data class Visitors(
  @Schema(description = "Person ID (nomis) of the visitor", example = "1234")
  val contactId: Long,
  @Schema(description = "true if visitor is the contact for the visit otherwise false", example = "true")
  val visitContact: Boolean?,
)

data class VisitContact(
  @Schema(description = "Contact name", example = "John Smith", required = true)
  @field:NotBlank
  val name: String,
  @Schema(description = "Contact Phone Number", example = "01234 567890", required = false)
  val telephone: String?,
  @Schema(description = "Contact Email Address", example = "email@example.com", required = false)
  val email: String?,
)

data class VisitorSupport(
  @Schema(description = "Support text description", example = "Visually impaired assistance", required = true)
  @field:NotBlank
  val description: String,
)

data class VisitExternalSystemDetails(
  @Schema(description = "Client name", example = "client_name")
  val clientName: String?,
  @Schema(description = "Client visit reference", example = "Reference ID in the client system")
  val clientVisitReference: String?,
)
