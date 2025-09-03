package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetAddressesForPersonService(
  @Autowired val prisonApiGateway: PrisonApiGateway,
  @Autowired val getPersonService: GetPersonService,
  private val deliusGateway: NDeliusGateway,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<List<Address>> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = emptyList(), errors = personResponse.errors)
    }

    val addressesFromDelius = deliusGateway.getAddressesForPerson(hmppsId)

    if (hasErrorOtherThanEntityNotFound(addressesFromDelius)) {
      return Response(data = emptyList(), errors = addressesFromDelius.errors)
    }

    val nomisNumber = personResponse.data?.nomisNumber ?: return addressesFromDelius

    val addressesFromNomis = prisonApiGateway.getAddressesForPerson(id = nomisNumber)
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

  private fun hasErrorOtherThanEntityNotFound(addressesResponse: Response<List<Address>>): Boolean = addressesResponse.errors.isNotEmpty() && !addressesResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)
}
