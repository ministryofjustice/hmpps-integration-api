package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.caseNotes

data class CNSearchNotesRequest(
  val includeSensitive: Boolean? = true,
  val occurredFrom: String? = null,
  val occurredTo: String? = null,
  val page: Int? = null,
  val size: Int? = null,
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
      map["page"] = page.toInt()
    }
    if (size != null) {
      map["size"] = size.toInt()
    }
    if (sort != null) {
      map["sort"] = sort
    }
    return map
  }
}
