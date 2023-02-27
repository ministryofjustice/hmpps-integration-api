package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

@Service
class GetPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway
) {

  fun execute(firstName: String, lastName: String): List<Person?> {
    val personsFromPrisonerOffenderSearch = prisonerOffenderSearchGateway.getPrisoners(firstName, lastName)
    val personsFromProbationOffenderSearch = probationOffenderSearchGateway.getOffenders(firstName, lastName)

    return personsFromPrisonerOffenderSearch + personsFromProbationOffenderSearch
  }

  fun execute(id: String): Map<String, Person?>? {
    val personFromPrisonerOffenderSearch = prisonerOffenderSearchGateway.getPerson(id)
    val personFromNomis = nomisGateway.getPerson(id)
    val personFromProbationOffenderSearch = probationOffenderSearchGateway.getPerson(id)

    if (personFromPrisonerOffenderSearch == null && personFromNomis == null && personFromProbationOffenderSearch == null) {
      return null
    }

    return mapOf(
      "nomis" to personFromNomis,
      "prisonerOffenderSearch" to personFromPrisonerOffenderSearch,
      "probationOffenderSearch" to personFromProbationOffenderSearch
    )
  }
}
