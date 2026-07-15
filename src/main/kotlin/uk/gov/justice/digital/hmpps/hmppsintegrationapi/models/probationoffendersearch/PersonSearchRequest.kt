package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import org.springframework.web.util.UriComponentsBuilder

data class PersonSearchRequest(
  val firstName: String? = null,
  val surname: String? = null,
  val dateOfBirth: String? = null,
  val pncNumber: String? = null,
  val includeAliases: Boolean = false,
) {
  fun uriString(
    page: Int,
    pageSize: Int,
  ): String {
    val uri =
      UriComponentsBuilder
        .fromUriString("/search/people")
    uri.queryParam("page", page - 1)
    uri.queryParam("size", pageSize)
    return uri.toUriString()
  }

  fun toMap(): Map<String, String?> =
    mapOf(
      "firstName" to firstName,
      "surname" to surname,
      "dateOfBirth" to dateOfBirth,
      "pncNumber" to pncNumber,
      "includeAliases" to includeAliases.toString(),
    )
}
