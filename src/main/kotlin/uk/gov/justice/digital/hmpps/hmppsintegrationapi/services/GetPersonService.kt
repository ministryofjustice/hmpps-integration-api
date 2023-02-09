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
  fun execute(id: String): Map<String, Person?> {
    val personFromPrisonerOffenderSearch = prisonerOffenderSearchGateway.getPerson(id)
    val personFromNomisGateway = nomisGateway.getPerson(id)
    val personFromProbationOffenderSearch = probationOffenderSearchGateway.getPerson(id)

    return mapOf(
      "nomis" to personFromNomisGateway,
      "prisonerOffenderSearch" to personFromPrisonerOffenderSearch,
      "probationOffenderSearch" to personFromProbationOffenderSearch
    )
  }
}
