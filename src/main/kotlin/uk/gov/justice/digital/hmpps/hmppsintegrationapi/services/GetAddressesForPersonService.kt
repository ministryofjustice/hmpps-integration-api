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
    val errors = mutableListOf<UpstreamApiError>()
    // Get Nomis Number using getNomisNumber to perform prison filtering as well as the convert function.
    // If hmppsId is a CRN number, calls cpr to resolve the NOMS from the CRN
    // If CPR unsuccessful the noms number held in probation is used
    val nomisId = getPersonService.getNomisNumber(hmppsId, filters).also { errors.addAll(it.errors) }

    // If hmppsId is a Nomis number, calls cpr to resolve the CRN from the NOMS -
    // if CPR unsuccessful, probation search is used to find the CRN for the NOMS
    val crn = getPersonService.convert(hmppsId, GetPersonService.IdentifierType.CRN).also { errors.addAll(it.errors) }

    // If a nomis number is resolved - get addresses from prisons and collect any non 404 errors
    val prisonAddresses =
      nomisId.data
        ?.nomisNumber
        ?.let { id ->
          prisonApiGateway.getAddressesForPerson(id).also { errors.addAll(errorsWithoutNotFound(it.errors)) }
        }?.data ?: emptyList()

    // If a crn is resolved - get addresses from probation and collect any non 404 errors
    val probationAddresses =
      crn.data
        ?.let { crn ->
          deliusGateway.getAddressesForPerson(crn).also { errors.addAll(errorsWithoutNotFound(it.errors)) }
        }?.data ?: emptyList()

    // Return both sets of addresses and any errors
    return Response(data = prisonAddresses + probationAddresses, errors)
  }

  private fun errorsWithoutNotFound(errors: List<UpstreamApiError>) = errors.filter { it.type != UpstreamApiError.Type.ENTITY_NOT_FOUND }
}
