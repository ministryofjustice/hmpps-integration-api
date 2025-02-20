package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits

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

data class VisitNotes(
  val type: String,
  val text: String,
)

data class Visitors(
  val nomisPersonId: Long,
  val visitContact: Boolean?,
)

data class VisitContact(
  val name: String,
  val telephone: String,
  val email: String,
)

data class VisitorSupport(
  val description: String,
)
