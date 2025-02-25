package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class Visit(
  @JsonProperty("applicationReference")
  @Schema(description = "Application Reference", example = "dfs-wjs-eqr")
  val applicationReference: String?,
  @JsonProperty("reference")
  @Schema(description = "Visit Reference", example = "v9-d7-ed-7u")
  val reference: String?,
  @JsonProperty("sessionTemplateReference")
  @Schema(description = "Session Template Reference", example = "v9d.7ed.7u")
  val sessionTemplateReference: String?,
  @JsonProperty("prisonerId")
  @Schema(description = "Prisoner Id", example = "AF34567G")
  val prisonerId: String,
  @JsonProperty("prisonId")
  @Schema(description = "Prison Id", example = "MDI")
  val prisonId: String,
  @JsonProperty("prisonName")
  @Schema(description = "Prison Name", example = "Moorland (HMP & YOI)")
  val prisonName: String,
  @JsonProperty("visitRoom")
  @Schema(description = "Location of Visit ", example = "Visits Main Hall")
  val visitRoom: String,
  @JsonProperty("visitType")
  @Schema(description = "Type of Visit", example = "SOCIAL")
  val visitType: String,
  @JsonProperty("visitStatus")
  @Schema(description = "Visit Status", example = "BOOKED")
  val visitStatus: String,
  @JsonProperty("outcomeStatus")
  @Schema(description = "Outcome Status of Visit", example = "ADMINISTRATIVE_CANCELLATION")
  val outcomeStatus: String?,
  @JsonProperty("visitRestriction")
  @Schema(description = "Visit Restriction", example = "OPEN")
  val visitRestriction: String,
  @JsonProperty("startTimestamp")
  @Schema(description = "The date and time of the visit")
  val startTimestamp: String,
  @JsonProperty("endTimestamp")
  @Schema(description = "The finishing date and time of the visit")
  val endTimestamp: String,
  @JsonProperty("visitors")
  @Schema(description = "List of visitors associated with the visit")
  val visitors: List<Visitors>,
  @JsonProperty("visitNotes")
  @Schema(description = "Visit Notes")
  val visitNotes: List<VisitNotes>,
  @JsonProperty("visitContact")
  @Schema(description = "Contact associated with the visit")
  val visitContact: VisitContact,
  @JsonProperty("visitorSupport")
  @Schema(description = "Additional support associated with the visit")
  val visitorSupport: VisitorSupport,
  @JsonProperty("createdTimestamp")
  @Schema(description = "The visit created date and time")
  val createdTimestamp: String,
  @JsonProperty("modifiedTimestamp")
  @Schema(description = "The visit modified date and time")
  val modifiedTimestamp: String,
  @JsonProperty("firstBookedDateTime")
  @Schema(description = "Date the visit was first booked or migrated")
  val firstBookedDateTime: String,
)

data class VisitNotes(
  @JsonProperty("type")
  @Schema(description = "Note type", allowableValues = ["VISITOR_CONCERN", "VISIT_OUTCOMES", "VISIT_COMMENT", "STATUS_CHANGED_REASON"], example = "VISITOR_CONCERN")
  val type: String,
  @JsonProperty("text")
  @Schema(description = "Note Text", example = "Visitor is concerned that his mother in-law is coming!")
  val text: String,
)

data class Visitors(
  @JsonProperty("nomisPersonId")
  @Schema(description = "Person ID (nomis) of the visitor", example = "1234")
  val nomisPersonId: Long,
  @JsonProperty("visitContact")
  @Schema(description = "true if visitor is the contact for the visit otherwise false", example = "true")
  val visitContact: Boolean?,
)

data class VisitContact(
  @JsonProperty("name")
  @Schema(description = "Contact name", example = "John Smith")
  val name: String,
  @JsonProperty("telephone")
  @Schema(description = "Contact Phone Number", example = "01234 567890")
  val telephone: String,
  @JsonProperty("email")
  @Schema(description = "Contact Email Address", example = "email@example.com")
  val email: String,
)

data class VisitorSupport(
  @JsonProperty("description")
  @Schema(description = "Support text description", example = "Visually impaired assistance")
  val description: String,
)
