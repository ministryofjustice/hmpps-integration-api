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
    val personFromPrisonerOffenderSearch = prisonerOffenderSearchGateway.getPersons(pncId = pncId)
    val personFromProbationOffenderSearch = probationOffenderSearchGateway.getPerson(pncId)

    if (personFromPrisonerOffenderSearch.isEmpty() && personFromProbationOffenderSearch == null) {
      return null
    }

    return mapOf(
      "prisonerOffenderSearch" to personFromPrisonerOffenderSearch.first(),
      "probationOffenderSearch" to personFromProbationOffenderSearch,
    )
  }
}
