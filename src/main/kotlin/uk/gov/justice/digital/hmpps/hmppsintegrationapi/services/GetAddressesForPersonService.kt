package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address

@Service
class GetAddressesForPersonService(
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val nomisGateway: NomisGateway
) {
  fun execute(pncId: String): List<Address>? {
    val personFromPrisonerOffenderSearch = prisonerOffenderSearchGateway.getPerson(pncId) ?: return listOf()

    val addressesFromNomis = nomisGateway.getAddressesForPerson(personFromPrisonerOffenderSearch.prisonerId!!)
    val addressesFromProbationOffenderSearch = probationOffenderSearchGateway.getAddressesForPerson(pncId)

    if (addressesFromNomis == null && addressesFromProbationOffenderSearch == null)
      return null

    return addressesFromProbationOffenderSearch?.plus(addressesFromNomis.orEmpty())
  }
}
