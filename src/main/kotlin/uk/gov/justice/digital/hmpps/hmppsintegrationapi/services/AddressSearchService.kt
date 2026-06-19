package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AddressSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AddressSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class AddressSearchService(
  private val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) {
  fun addressSearch(
    addressSearchRequest: AddressSearchRequest,
    requestContext: RequestContext?,
  ): Response<AddressSearchResponse?> {
    val addressSearch = probationOffenderSearchGateway.addressSearch(addressSearchRequest, requestContext)
    val data = addressSearch.data ?: return Response(null, addressSearch.errors)
    return Response(data.toDownstreamFormat())
  }
}
