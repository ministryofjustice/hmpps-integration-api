package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address

@Service
class GetAddressesForPersonService(
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @Autowired val nomisGateway: NomisGateway
) {
  fun execute(id: String): List<Address> {
    val addressesFromProbationOffenderSearch = probationOffenderSearchGateway.getAddressesForPerson(id)
    val addressesFromNomis = nomisGateway.getAddressesForPerson(id)

    return addressesFromProbationOffenderSearch + addressesFromNomis
  }
}
