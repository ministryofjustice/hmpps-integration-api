package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.v2

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CorePersonRecordGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecordSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecordSearchResponseItem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class PersonSearchService(
  private val corePersonRecordGateway: CorePersonRecordGateway,
) {
  fun personSearch(
    request: CorePersonRecordSearchRequest,
    requestContext: RequestContext? = null,
  ): Response<List<CorePersonRecordSearchResponseItem>?> {
    val response = corePersonRecordGateway.corePersonRecordSearch(request, requestContext)
    if (response.errors.isNotEmpty()) {
      return Response(data = null, errors = response.errors)
    }
    return Response(data = response.data?.data)
  }
}
