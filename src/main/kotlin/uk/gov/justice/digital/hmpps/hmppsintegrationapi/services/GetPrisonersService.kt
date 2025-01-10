package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetPrisonersService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
) {
  fun execute(
    firstName: String?,
    lastName: String?,
    dateOfBirth: String?,
    searchWithinAliases: Boolean = false,
    filters: ConsumerFilters?,
  ): Response<List<Person>> {
    val prisonIds = filters?.prisons
    val responseFromPrisonerOffenderSearch =
      if (prisonIds.isNullOrEmpty()) {
        // Hit global-search endpoint
        prisonerOffenderSearchGateway.getPersons(
          firstName,
          lastName,
          dateOfBirth,
          searchWithinAliases,
        )
      } else {
        // Hit prisoner-details endpoint
        prisonerOffenderSearchGateway.getPrisonerByCriteria(firstName, lastName, dateOfBirth, searchWithinAliases, prisonIds)
      }

    if (responseFromPrisonerOffenderSearch.errors.isNotEmpty()) {
      return Response(emptyList(), responseFromPrisonerOffenderSearch.errors)
    }

    if (responseFromPrisonerOffenderSearch.data.isEmpty()) {
      return Response(emptyList(), listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
    }

    if (!prisonIds.isNullOrEmpty()) {
      // Check each prisoner has a valid prisonId that matches the consumers filter config, if prison filter exists against the consumer
      responseFromPrisonerOffenderSearch.data.forEach { prisoner ->
        if (
          !filters.matchesPrison(prisoner.prisonId)
        ) {
          return Response(
            data = emptyList(),
            errors = listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")),
          )
        }
      }
    }

    return Response(data = responseFromPrisonerOffenderSearch.data)
  }
}
