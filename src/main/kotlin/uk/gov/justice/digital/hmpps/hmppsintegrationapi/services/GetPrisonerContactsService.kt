package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRPrisonerContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PaginatedContactDetails

@Service
class GetPrisonerContactsService(
  @Autowired val personalRelationshipsGateway: PersonalRelationshipsGateway,
  @Autowired val mapper: ObjectMapper,
) {
  fun execute(
    prisonerId: String,
    page: Int,
    size: Int,
  ): PaginatedContactDetails? {
    // TODO: Wrap in response obj for errors field
    // TODO: get prison id from nomis etc consumer filters bla bla
    val response = personalRelationshipsGateway.getContacts(prisonerId, page, size)

    // construct paginated response

    var paginatedResponse = response.data?.toPaginatedContactDetails()

//    if (!response.data?.empty) {
    if (paginatedResponse != null) {
      paginatedResponse.contacts = mapToContactDetails(response.data!!.contacts)
    }
//    }

    return paginatedResponse
  }

  fun mapToContactDetails(result: List<PRPrisonerContact>): List<PrisonerContact> {
    val mappedResult: List<PrisonerContact> = mapper.convertValue(result, object : TypeReference<List<PrisonerContact>>() {})
    return mappedResult
  }
}
