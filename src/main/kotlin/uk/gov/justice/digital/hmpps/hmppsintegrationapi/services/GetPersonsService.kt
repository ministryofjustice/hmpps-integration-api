package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetPersonsService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) {

  fun execute(firstName: String?, lastName: String?, pncNumber: String?, dateOfBirth: String?, searchWithinAliases: Boolean = false): Response<List<Person>> {
    var hmppsId: String? = null

    val personsFromProbationOffenderSearch = probationOffenderSearchGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth, searchWithinAliases)

    if (!pncNumber.isNullOrEmpty()) {
      hmppsId = personsFromProbationOffenderSearch.data.firstOrNull()?.identifiers?.deliusCrn
    }

    val responseFromPrisonerOffenderSearch = prisonerOffenderSearchGateway.getPersons(firstName, lastName, hmppsId, dateOfBirth, searchWithinAliases)
    return Response(data = responseFromPrisonerOffenderSearch.data + personsFromProbationOffenderSearch.data)
  }
}
