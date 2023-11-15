package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response

@Service
class GetAddressesForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) {
  fun execute(pncId: String): Response<List<Address>> {
    val personResponse = getPersonService.execute(hmppsId = pncId)
    val nomisNumber = personResponse.data?.identifiers?.nomisNumber
    val deliusCrn = personResponse.data?.identifiers?.deliusCrn
    var addressesFromNomis: Response<List<Address>> = Response(data = emptyList())
    var addressesFromDelius: Response<List<Address>> = Response(data = emptyList())

    if (nomisNumber != null) {
      addressesFromNomis = nomisGateway.getAddressesForPerson(id = nomisNumber)
    }

    if (deliusCrn != null) {
      addressesFromDelius = probationOffenderSearchGateway.getAddressesForPerson(pncId)
    }

    return Response.merge(listOf(addressesFromNomis, addressesFromDelius))
  }
}
