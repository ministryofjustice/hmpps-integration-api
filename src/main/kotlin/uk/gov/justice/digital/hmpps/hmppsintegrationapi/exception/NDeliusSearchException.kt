package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

data class NDeliusSearchException(
  val crnId: String,
  override val errors: List<UpstreamApiError>,
) : UpstreamApiException("person", crnId, errors)
