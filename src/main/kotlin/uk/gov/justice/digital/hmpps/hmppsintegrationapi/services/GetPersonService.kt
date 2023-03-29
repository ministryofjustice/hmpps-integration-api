package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

@Service
class GetPersonService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) {
  fun execute(pncId: String): Map<String, Person?>? {
    val personsFromPrisonerOffenderSearch = prisonerOffenderSearchGateway.getPersons(pncId = pncId)
    val personFromProbationOffenderSearch = probationOffenderSearchGateway.getPerson(pncId)

    if (personsFromPrisonerOffenderSearch.isEmpty() && personFromProbationOffenderSearch == null) {
      return null
    }

    val personFromPrisonerOffenderSearch = if (personsFromPrisonerOffenderSearch.isEmpty()) null else personsFromPrisonerOffenderSearch.first()

    return mapOf(
      "prisonerOffenderSearch" to personFromPrisonerOffenderSearch,
      "probationOffenderSearch" to personFromProbationOffenderSearch,
    )
  }
}
