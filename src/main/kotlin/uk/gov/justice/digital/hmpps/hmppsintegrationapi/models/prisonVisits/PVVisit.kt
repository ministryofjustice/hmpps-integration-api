package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitExternalSystemDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitNotes
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitorSupport
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visitors

data class PVVisit(
  @JsonProperty("applicationReference")
  val applicationReference: String?,
  @JsonProperty("reference")
  val reference: String?,
  @JsonProperty("prisonerId")
  val prisonerId: String,
  @JsonProperty("prisonId")
  val prisonId: String,
  @JsonProperty("prisonName")
  val prisonName: String?,
  @JsonProperty("sessionTemplateReference")
  val sessionTemplateReference: String?,
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
  @JsonProperty("visitNotes")
  val visitNotes: List<PVVisitNotes>?,
  @JsonProperty("visitContact")
  val visitContact: PVVisitContact?,
  @JsonProperty("visitors")
  val visitors: List<PVVisitors>?,
  @JsonProperty("visitorSupport")
  val visitorSupport: PVVisitorSupport?,
  @JsonProperty("createdTimestamp")
  val createdTimestamp: String,
  @JsonProperty("modifiedTimestamp")
  val modifiedTimestamp: String,
  @JsonProperty("firstBookedDateTime")
  val firstBookedDateTime: String,
  @JsonProperty("visitExternalSystemDetails")
  val visitExternalSystemDetails: PVVistExternalSystemDetails?,
) {
  fun toVisit(): Visit =
    Visit(
      applicationReference = this.applicationReference,
      reference = this.reference,
      prisonerId = this.prisonerId,
      prisonId = this.prisonId,
      prisonName = this.prisonName,
      sessionTemplateReference = this.sessionTemplateReference,
      visitRoom = this.visitRoom,
      visitType = this.visitType,
      visitStatus = this.visitStatus,
      outcomeStatus = this.outcomeStatus,
      visitRestriction = this.visitRestriction,
      startTimestamp = this.startTimestamp,
      endTimestamp = this.endTimestamp,
      visitNotes = this.visitNotes?.map { it.toVisitNotes() }.orEmpty(),
      visitContact = this.visitContact?.toVisitContact(),
      visitors = this.visitors?.map { it.toVisitors() }.orEmpty(),
      visitorSupport = this.visitorSupport?.toVisitorSupport(),
      visitExternalSystemDetails = this.visitExternalSystemDetails?.toVisitExternalSystemDetails(),
      createdTimestamp = this.createdTimestamp,
      modifiedTimestamp = this.modifiedTimestamp,
      firstBookedDateTime = this.firstBookedDateTime,
    )
}

data class PVVisitNotes(
  @JsonProperty("type")
  val type: String,
  @JsonProperty("text")
  val text: String,
) {
  fun toVisitNotes(): VisitNotes = VisitNotes(type = this.type, text = this.text)
}

data class PVVisitors(
  @JsonProperty("nomisPersonId")
  val nomisPersonId: Long,
  @JsonProperty("visitContact")
  val visitContact: Boolean?,
) {
  fun toVisitors(): Visitors = Visitors(contactId = this.nomisPersonId, visitContact = this.visitContact)
}

data class PVVisitContact(
  @JsonProperty("name")
  val name: String,
  @JsonProperty("telephone")
  val telephone: String,
  @JsonProperty("email")
  val email: String,
) {
  fun toVisitContact(): VisitContact = VisitContact(name = this.name, telephone = this.telephone, email = this.email)
}

data class PVVisitorSupport(
  @JsonProperty("description")
  val description: String,
) {
  fun toVisitorSupport(): VisitorSupport = VisitorSupport(description = this.description)
}

data class PVVistExternalSystemDetails(
  @JsonProperty("clientName")
  val clientName: String?,
  @JsonProperty("clientVisitReference")
  val clientVisitReference: String?,
) {
  fun toVisitExternalSystemDetails(): VisitExternalSystemDetails = VisitExternalSystemDetails(clientName = this.clientName, clientVisitReference = this.clientVisitReference)
}
