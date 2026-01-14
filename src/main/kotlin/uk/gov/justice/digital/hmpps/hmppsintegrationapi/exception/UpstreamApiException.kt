package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

open class UpstreamApiException(
  val resourceType: String? = "resource",
  val resourceId: String,
  open val errors: List<UpstreamApiError>,
) : RuntimeException("Could not find resource with id: $resourceId")
