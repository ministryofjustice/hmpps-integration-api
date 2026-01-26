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
  ): Response<List<Address>> =
    Response.merge(
      listOf(
        getPrisonAddresses(hmppsId, filters),
        getProbationAddresses(hmppsId, filters),
      )
    )


  private fun getProbationAddresses(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<List<Address>> {
    if (!hasProbationAccess(filters)) return Response(emptyList())

    val crn = getPersonService.convert(hmppsId, GetPersonService.IdentifierType.CRN)

    if (crn.data == null || crn.errors.isNotEmpty()) return errorResponse(crn.errors)

    return removeNotFoundErrors(deliusGateway.getAddressesForPerson(crn.data))
  }

  private fun getPrisonAddresses(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<List<Address>> {
    if (!hasPrisonsAccess(filters)) return Response(emptyList())

    val nomisId = getPersonService.getNomisNumber(hmppsId, filters)

    if (nomisId.data?.nomisNumber == null || nomisId.errors.isNotEmpty()) return errorResponse(nomisId.errors)

    return removeNotFoundErrors(prisonApiGateway.getAddressesForPerson(nomisId.data.nomisNumber))
  }

  // Move to Response.kt?
  private fun removeNotFoundErrors(response: Response<List<Address>>) = Response(
    response.data,
    response.errors.filter { it.type != UpstreamApiError.Type.ENTITY_NOT_FOUND },
  )
  private fun errorResponse(errors: List<UpstreamApiError>): Response<List<Address>> = Response(emptyList(), errors)

  // Move to ConsumerFilters.kt?
  private fun hasProbationAccess(filters: ConsumerFilters?) = filters?.supervisionStatuses == null || filters.hasProbation()
  private fun hasPrisonsAccess(filters: ConsumerFilters?) = filters?.supervisionStatuses == null || filters.hasPrisons()
}

