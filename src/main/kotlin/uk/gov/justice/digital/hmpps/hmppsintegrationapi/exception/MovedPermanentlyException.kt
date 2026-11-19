package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.identifyHmppsId

data class MovedPermanentlyException(
  override val message: String,
  val location: String? = null,
) : RuntimeException(message) {
  fun toLocation(reqPath: String): String {
    val hmppsId =
      this.location?.let { loc ->
        loc.split("/").firstOrNull {
          identifyHmppsId(it) != GetPersonService.IdentifierType.UNKNOWN
        }
      }
    val downstreamLocation = if (hmppsId != null) reqPath.replace("{hmppsId}", hmppsId) else reqPath
    return downstreamLocation
  }
}
