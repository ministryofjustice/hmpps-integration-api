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
    if (hasErrorOtherThanEntityNotFound(addressesFromDelius)) {
      return Response(data = emptyList(), errors = addressesFromDelius.errors)
    }

    val nomisNumber = personResponse.data?.identifiers?.nomisNumber
    if (nomisNumber == null) {
      return addressesFromDelius
    }

    val addressesFromNomis = nomisGateway.getAddressesForPerson(id = nomisNumber)
    if (hasErrorOtherThanEntityNotFound(addressesFromNomis)) {
      return Response(data = emptyList(), errors = addressesFromNomis.errors)
    }

    if (
      addressesFromNomis.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND) &&
      !addressesFromDelius.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)
    ) {
      return addressesFromDelius
    }

    return Response.merge(listOfNotNull(addressesFromNomis, Response(data = addressesFromDelius.data)))
  }

  private fun hasErrorOtherThanEntityNotFound(addressesResponse: Response<List<Address>>): Boolean = addressesResponse.errors.isNotEmpty() && addressesResponse.errors.none { it.type == UpstreamApiError.Type.ENTITY_NOT_FOUND }
}
