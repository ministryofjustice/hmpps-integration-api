package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.caseNotes

data class CNSearchNotesRequest(
  val includeSensitive: Boolean? = true,
  val occurredFrom: String? = null,
  val occurredTo: String? = null,
  // confirm pagination is not required
  val page: String? = null,
  val size: String? = null,
  val sort: String? = null,
) {
  fun toApiConformingMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    if (includeSensitive != null) {
      map["includeSensitive"] = includeSensitive
    }
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
