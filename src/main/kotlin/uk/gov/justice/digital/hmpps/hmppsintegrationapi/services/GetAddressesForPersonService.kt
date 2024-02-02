package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetAddressesForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) {
  fun execute(hmppsId: String): Response<List<Address>> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val nomisNumber = personResponse.data?.identifiers?.nomisNumber

    var addressesFromNomis: Response<List<Address>> = Response(data = emptyList())
    val addressesFromDelius = probationOffenderSearchGateway.getAddressesForPerson(hmppsId = hmppsId)

    if (nomisNumber != null) {
      addressesFromNomis = nomisGateway.getAddressesForPerson(id = nomisNumber)
    }

    return Response.merge(listOfNotNull(addressesFromNomis, addressesFromDelius))
  }
}
