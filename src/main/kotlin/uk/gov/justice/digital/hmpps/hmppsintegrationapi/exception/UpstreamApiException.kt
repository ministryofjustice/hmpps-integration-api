package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

open class UpstreamApiException(
  val upstreamApi: UpstreamApi,
  val errorType: UpstreamApiError.Type,
  val resourceType: String? = "resource",
  val resourceId: String?,
  open val errors: List<UpstreamApiError>,
) : RuntimeException("[$errorType] error occurred in upstream API: [$upstreamApi] while requesting [$resourceType] with id [$resourceId]")
