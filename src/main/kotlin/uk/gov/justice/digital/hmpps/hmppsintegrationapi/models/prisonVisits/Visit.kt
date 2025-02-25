package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits

import com.fasterxml.jackson.annotation.JsonProperty

data class Visit(
  @JsonProperty("applicationReference")
  val applicationReference: String?,
  @JsonProperty("reference")
  val reference: String?,
  @JsonProperty("sessionTemplateReference")
  val sessionTemplateReference: String?,
  @JsonProperty("prisonerId")
  val prisonerId: String,
  @JsonProperty("prisonId")
  val prisonId: String,
  @JsonProperty("prisonName")
  val prisonName: String,
  @JsonProperty("visitRoom")
  val visitRoom: String,
  @JsonProperty("visitType")
  val visitType: String,
  @JsonProperty("visitStatus")
  val visitStatus: String,
  @JsonProperty("outcomeStatus")
  val outcomeStatus: String?,
  @JsonProperty("visitRestriction")
  val visitRestriction: String,
  @JsonProperty("startTimestamp")
  val startTimestamp: String,
  @JsonProperty("endTimestamp")
  val endTimestamp: String,
  @JsonProperty("visitors")
  val visitors: List<Visitors>,
  @JsonProperty("visitNotes")
  val visitNotes: List<VisitNotes>,
  @JsonProperty("visitContact")
  val visitContact: VisitContact,
  @JsonProperty("visitorSupport")
  val visitorSupport: VisitorSupport,
  @JsonProperty("createdTimestamp")
  val createdTimestamp: String,
  @JsonProperty("modifiedTimestamp")
  val modifiedTimestamp: String,
  @JsonProperty("firstBookedDateTime")
  val firstBookedDateTime: String,
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
