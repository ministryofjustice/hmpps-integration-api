package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.caseNotes

data class CNSearchNotesRequest(
  val occurredFrom: String? = null,
  val occurredTo: String? = null,
  // confirm pagination is not required
  val page: String? = null,
  val size: String? = null,
  val sort: String? = null,
) {
  fun toApiConformingMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    if (occurredFrom != null) {
      map["occurredFrom"] = occurredFrom
    }
    if (occurredTo != null) {
      map["occurredTo"] = occurredTo
    }
    if (page != null) {
      map["page"] = page
    }
    if (size != null) {
      map["size"] = size
    }
    if (sort != null) {
      map["sort"] = sort
    }
    return map
  }
}
