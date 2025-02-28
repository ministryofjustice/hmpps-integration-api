package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedPrisonerContacts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetPrisonerContactsService(
  @Autowired val personalRelationshipsGateway: PersonalRelationshipsGateway,
) {
  fun execute(
    prisonerId: String,
    page: Int,
    size: Int,
    filter: ConsumerFilters?,
  ): Response<PaginatedPrisonerContacts?> {
    // TODO: Wrap in response obj for errors field
    // TODO: get prison id from nomis etc consumer filters bla bla
    val response = personalRelationshipsGateway.getContacts(prisonerId, page, size)
    if (response.errors.isNotEmpty()) {
      return Response(data = null, errors = response.errors)
    }

    return Response(data = response.data?.toPaginatedPrisonerContacts())
  }
}
