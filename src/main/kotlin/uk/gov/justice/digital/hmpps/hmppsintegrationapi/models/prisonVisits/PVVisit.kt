package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitExternalSystemDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitNotes
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitorSupport
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visitors

data class PVVisit(
  val applicationReference: String?,
  val reference: String?,
  val prisonerId: String,
  val prisonId: String,
  val prisonName: String?,
  val sessionTemplateReference: String?,
  val visitRoom: String,
  val visitType: String,
  val visitStatus: String,
  val outcomeStatus: String?,
  val visitRestriction: String,
  val startTimestamp: String,
  val endTimestamp: String,
  val visitNotes: List<PVVisitNotes>?,
  val visitContact: PVVisitContact?,
  val visitors: List<PVVisitors>?,
  val visitorSupport: PVVisitorSupport?,
  val createdTimestamp: String,
  val modifiedTimestamp: String,
  val firstBookedDateTime: String?,
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
  val type: String,
  val text: String,
) {
  fun toVisitNotes(): VisitNotes = VisitNotes(type = this.type, text = this.text)
}

data class PVVisitors(
  val nomisPersonId: Long,
  val visitContact: Boolean?,
) {
  fun toVisitors(): Visitors = Visitors(contactId = this.nomisPersonId, visitContact = this.visitContact)
}

data class PVVisitContact(
  val name: String,
  val telephone: String?,
  val email: String?,
) {
  fun toVisitContact(): VisitContact = VisitContact(name = this.name, telephone = this.telephone, email = this.email)
}

data class PVVisitorSupport(
  val description: String,
) {
  fun toVisitorSupport(): VisitorSupport = VisitorSupport(description = this.description)
}

data class PVVistExternalSystemDetails(
  val clientName: String?,
  val clientVisitReference: String?,
) {
  fun toVisitExternalSystemDetails(): VisitExternalSystemDetails = VisitExternalSystemDetails(clientName = this.clientName, clientVisitReference = this.clientVisitReference)
}
