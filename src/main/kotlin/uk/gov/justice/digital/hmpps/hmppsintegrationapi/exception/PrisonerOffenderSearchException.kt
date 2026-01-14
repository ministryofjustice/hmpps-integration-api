package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

data class PrisonerOffenderSearchException(
  val hmppsId: String,
  override val errors: List<UpstreamApiError>,
) : UpstreamApiException("person", hmppsId, errors)
