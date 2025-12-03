package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.SearchUtils.attributeSearchRequest

@Service
class GetPersonsService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  private val deliusGateway: NDeliusGateway,
) {
  fun execute(
    firstName: String?,
    lastName: String?,
    pncNumber: String?,
    dateOfBirth: String?,
    searchWithinAliases: Boolean = false,
  ): Response<List<Person>> {
    val responseFromProbationOffenderSearch =
      deliusGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth, searchWithinAliases)

    if (responseFromProbationOffenderSearch.errors.isNotEmpty()) {
      return Response(emptyList(), responseFromProbationOffenderSearch.errors)
    }

    if (pncNumber.isNullOrBlank()) {
      val responseFromPrisonerOffenderSearch =
        prisonerOffenderSearchGateway.getPersons(
          firstName,
          lastName,
          dateOfBirth,
          searchWithinAliases,
        )
      return Response(data = responseFromPrisonerOffenderSearch.data.map { it.toPerson() } + responseFromProbationOffenderSearch.data)
    }
    return Response(data = responseFromProbationOffenderSearch.data)
  }

  /**
   * Enhanced person search using prisonerOffenderSearchGateway.attributeSearch
   */
  fun personAttributeSearch(
    firstName: String?,
    lastName: String?,
    pncNumber: String?,
    dateOfBirth: String?,
    searchWithinAliases: Boolean = false,
    consumerFilters: ConsumerFilters? = null,
  ): Response<List<Person>> {
    // Perform probation search
    val probationSearchResponse =
      if (consumerFilters?.isPrisonsOnly() != true) {
        deliusGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth, searchWithinAliases)
      } else {
        Response(emptyList(), emptyList())
      }

    // Perform prison search
    val attributeSearchRequest = attributeSearchRequest(firstName, lastName, pncNumber, dateOfBirth, searchWithinAliases, consumerFilters)
    val attributeSearchResponse = prisonerOffenderSearchGateway.attributeSearch(attributeSearchRequest)
    val attributeSearchResponseData = attributeSearchResponse.data?.content?.map { it.toPerson() } ?: emptyList()

    // Combine and return results
    val errors = attributeSearchResponse.errors + probationSearchResponse.errors
    val data = attributeSearchResponseData + probationSearchResponse.data

    return Response(data, errors)
  }
}
