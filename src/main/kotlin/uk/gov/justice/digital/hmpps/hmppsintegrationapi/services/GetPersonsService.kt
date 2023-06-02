package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

@Service
class GetPersonsService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) {

  fun execute(firstName: String?, lastName: String?): List<Person?> {
    val responseFromPrisonerOffenderSearch = prisonerOffenderSearchGateway.getPersons(firstName, lastName)
    val personsFromProbationOffenderSearch = probationOffenderSearchGateway.getPersons(firstName, lastName)

    return responseFromPrisonerOffenderSearch.data + personsFromProbationOffenderSearch.data
  }
}
