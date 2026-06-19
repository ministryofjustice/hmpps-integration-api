package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import jakarta.validation.ValidationException
import org.springframework.web.util.UriComponentsBuilder

data class AddressSearchRequest(
  val buildingName: String? = null,
  val addressNumber: String? = null,
  val streetName: String? = null,
  val postcode: String? = null,
  val maxResults: Int? = null,
) {
  fun uriString(): String {
    val uri =
      UriComponentsBuilder
        .fromUriString("/search/addresses")
    maxResults?.let { uri.queryParam("maxResults", it) }
    return uri.toUriString()
  }

  fun validate() {
    if (buildingName == null && addressNumber == null && streetName == null && postcode == null) {
      throw ValidationException("No search criteria found")
    }
  }

  fun toMap(): Map<String, String?> =
    mapOf(
      "buildingName" to buildingName,
      "addressNumber" to addressNumber,
      "streetName" to streetName,
      "postcode" to postcode,
    )
}
