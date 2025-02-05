package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@Service
class GetAddressesForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) {
  fun execute(hmppsId: String): Response<List<Address>> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = emptyList(), errors = personResponse.errors)
    }

    val addressesFromDelius = probationOffenderSearchGateway.getAddressesForPerson(hmppsId = hmppsId)
    if (addressesFromDelius.errors.isNotEmpty() && addressesFromDelius.errors.none { it.type == UpstreamApiError.Type.ENTITY_NOT_FOUND }) {
      return Response(data = emptyList(), errors = addressesFromDelius.errors)
    }

    val nomisNumber = personResponse.data?.identifiers?.nomisNumber

    if (nomisNumber == null) {
      return addressesFromDelius
    }

    val addressesFromNomis = nomisGateway.getAddressesForPerson(id = nomisNumber)
    if (addressesFromNomis.errors.isNotEmpty() && addressesFromNomis.errors.none { it.type == UpstreamApiError.Type.ENTITY_NOT_FOUND }) {
      return Response(data = emptyList(), errors = addressesFromNomis.errors)
    }

    if (
      addressesFromNomis.errors.isNotEmpty() &&
      addressesFromNomis.errors.any { it.type == UpstreamApiError.Type.ENTITY_NOT_FOUND } &&
      addressesFromDelius.errors.isEmpty()
    ) {
      return addressesFromDelius
    }

    return Response.merge(listOfNotNull(addressesFromNomis, Response(data = addressesFromDelius.data)))
  }
}
