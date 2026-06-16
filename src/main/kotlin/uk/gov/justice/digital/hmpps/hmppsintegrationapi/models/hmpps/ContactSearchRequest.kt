package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import jakarta.validation.ValidationException
import org.springframework.web.util.UriComponentsBuilder
import java.text.ParseException
import java.text.SimpleDateFormat

data class ContactSearchRequest(
  val firstName: String? = null,
  val middleNames: String? = null,
  val lastName: String? = null,
  val dateOfBirth: String? = null,
  val searchType: ContactSearchType = ContactSearchType.EXACT,
) {
  fun uriString(
    pageNo: Int,
    perPage: Int,
  ): String {
    val uri =
      UriComponentsBuilder
        .fromUriString("/contacts/search")
    // Downstream pagination is zero based
    uri.queryParam("page", (pageNo - 1).toString())
    uri.queryParam("size", perPage.toString())
    firstName?.let { uri.queryParam("firstName", it) }
    middleNames?.let { uri.queryParam("middleNames", it) }
    lastName?.let { uri.queryParam("lastName", it) }
    dateOfBirth?.let { uri.queryParam("dateOfBirth", it) }
    uri.queryParam("searchType", searchType.name)
    return uri.toUriString()
  }

  fun validate() {
    if (firstName == null && middleNames == null && lastName == null && dateOfBirth == null) {
      throw ValidationException("No search criteria found")
    }
    if (!isValidDate(dateOfBirth)) {
      throw ValidationException("dateOfBirth invalid format (should be dd/mm/yyyy)")
    }
  }

  fun isValidDate(date: String?): Boolean {
    dateOfBirth?.let {
      try {
        SimpleDateFormat("dd/mm/yyyy").parse(it)
      } catch (_: ParseException) {
        return false
      }
    }
    return true
  }

  fun toMap(): Map<String, String?> =
    mapOf(
      "firstName" to firstName,
      "middleNames" to middleNames,
      "lastName" to lastName,
      "dateOfBirth" to dateOfBirth,
      "searchType" to searchType.name,
    )
}

enum class ContactSearchType {
  EXACT,
  PARTIAL,
  SOUNDS_LIKE,
}
