package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetPrisonersService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val request: HttpServletRequest,
) {
  fun execute(
    firstName: String?,
    lastName: String?,
    dateOfBirth: String?,
    searchWithinAliases: Boolean = false,
  ): Response<List<Person>> {
    val requestFilters = request.getAttribute("filters") as ConsumerFilters
    // hmmm
    val prisonIds = requestFilters.prisons

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

    return Response(data = responseFromPrisonerOffenderSearch.data)
  }
}
