package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits

import com.fasterxml.jackson.annotation.JsonProperty

data class Visit(
  val prisonerId: String,
  val prisonId: String,
  val prisonName: String?,
  val visitRoom: String,
  val visitType: String,
  val visitStatus: String,
  val outcomeStatus: String?,
  val visitRestriction: String,
  val startTimestamp: String,
  val endTimestamp: String,
  val createdTimestamp: String,
  val modifiedTimestamp: String,
  val firstBookedDateTime: String,
  val visitors: List<Visitors>,
  val visitNotes: List<VisitNotes>,
  val visitContact: VisitContact,
  val visitorSupport: VisitorSupport,
)

data class FutureVisit(
  @JsonProperty("applicationReference")
  val applicationReference: String,
  @JsonProperty("reference")
  val reference: String,
  @JsonProperty("prisonerId")
  val prisonerId: String,
  @JsonProperty("prisonId")
  val prisonId: String,
  @JsonProperty("prisonName")
  val prisonName: String,
  @JsonProperty("sessionTemplateReference")
  val sessionTemplateReference: String,
  @JsonProperty("visitRoom")
  val visitRoom: String,
  @JsonProperty("visitType")
  val visitType: String,
  @JsonProperty("visitStatus")
  val visitStatus: String,
  @JsonProperty("outcomeStatus")
  val outcomeStatus: String,
  @JsonProperty("visitRestriction")
  val visitRestriction: String,
  @JsonProperty("startTimestamp")
  val startTimestamp: String,
  @JsonProperty("endTimestamp")
  val endTimestamp: String,
  @JsonProperty("createdTimestamp")
  val createdTimestamp: String,
  @JsonProperty("modifiedTimestamp")
  val modifiedTimestamp: String,
  @JsonProperty("firstBookedDateTime")
  val firstBookedDateTime: String,
  @JsonProperty("visitNotes")
  val visitNotes: List<VisitNotes>,
  @JsonProperty("visitContact")
  val visitContact: VisitContact,
  @JsonProperty("visitors")
  val visitors: List<Visitors>,
  @JsonProperty("visitorSupport")
  val visitorSupport: VisitorSupport,
)

data class VisitNotes(
  @JsonProperty("type")
  val type: String,
  @JsonProperty("text")
  val text: String,
)

data class Visitors(
  @JsonProperty("nomisPersonId")
  val nomisPersonId: Long,
  @JsonProperty("visitContact")
  val visitContact: Boolean?,
)

data class VisitContact(
  @JsonProperty("name")
  val name: String,
  @JsonProperty("telephone")
  val telephone: String,
  @JsonProperty("email")
  val email: String,
)

data class VisitorSupport(
  @JsonProperty("description")
  val description: String,
)
