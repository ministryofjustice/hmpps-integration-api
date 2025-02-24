package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits

import com.fasterxml.jackson.annotation.JsonProperty

data class Visit(
  @JsonProperty("applicationReference")
  val applicationReference: String? = null,
  @JsonProperty("reference")
  val reference: String? = null,
  @JsonProperty("prisonerId")
  val prisonerId: String,
  @JsonProperty("prisonId")
  val prisonId: String,
  @JsonProperty("prisonName")
  val prisonName: String? = null,
  @JsonProperty("sessionTemplateReference")
  val sessionTemplateReference: String? = null,
  @JsonProperty("visitRoom")
  val visitRoom: String,
  @JsonProperty("visitType")
  val visitType: String,
  @JsonProperty("visitStatus")
  val visitStatus: String,
  @JsonProperty("outcomeStatus")
  val outcomeStatus: String? = null,
  @JsonProperty("visitRestriction")
  val visitRestriction: String,
  @JsonProperty("startTimestamp")
  val startTimestamp: String,
  @JsonProperty("endTimestamp")
  val endTimestamp: String,
  @JsonProperty("createdTimestamp")
  val createdTimestamp: String? = null,
  @JsonProperty("modifiedTimestamp")
  val modifiedTimestamp: String? = null,
  @JsonProperty("firstBookedDateTime")
  val firstBookedDateTime: String? = null,
  @JsonProperty("visitNotes")
  val visitNotes: List<VisitNotes>? = null,
  @JsonProperty("visitContact")
  val visitContact: VisitContact? = null,
  @JsonProperty("visitors")
  val visitors: List<Visitors>? = null,
  @JsonProperty("visitorSupport")
  val visitorSupport: VisitorSupport? = null,
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
  val visitContact: Boolean? = null,
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
