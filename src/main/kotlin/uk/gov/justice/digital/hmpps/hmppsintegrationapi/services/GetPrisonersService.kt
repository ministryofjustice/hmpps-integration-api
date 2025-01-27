package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
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
  ): Response<List<PersonInPrison>> {
    val prisonIds = filters?.prisons
    val responseFromPrisonerOffenderSearch =
      if (prisonIds == null) {
        // Hit global-search endpoint
        prisonerOffenderSearchGateway.getPersons(
          firstName,
          lastName,
          dateOfBirth,
          searchWithinAliases,
        )
      } else {
        // Hit prisoner-details endpoint
        prisonerOffenderSearchGateway.getPrisonerDetails(firstName, lastName, dateOfBirth, searchWithinAliases, prisonIds)
      }

    if (responseFromPrisonerOffenderSearch.errors.isNotEmpty()) {
      return Response(emptyList(), responseFromPrisonerOffenderSearch.errors)
    }

    if (responseFromPrisonerOffenderSearch.data.isEmpty()) {
      return Response(emptyList(), listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
    }

    return Response(data = responseFromPrisonerOffenderSearch.data.map { it.toPersonInPrison() })
  }
}
