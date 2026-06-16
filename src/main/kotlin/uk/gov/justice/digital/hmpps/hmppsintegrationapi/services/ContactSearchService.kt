package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedContactSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class ContactSearchService(
  private val personalRelationshipsGateway: PersonalRelationshipsGateway,
) {
  fun contactSearch(
    contactSearchRequest: ContactSearchRequest,
    pageNo: Int,
    perPage: Int,
    requestContext: RequestContext?,
  ): Response<PaginatedContactSearchResponse?> {
    val contactSearch = personalRelationshipsGateway.contactSearch(contactSearchRequest, pageNo, perPage, requestContext)
    val data = contactSearch.data ?: return Response(null, contactSearch.errors)
    return Response(data.toPaginatedContactSearchResponse())
  }
}
