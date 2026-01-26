package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.withoutNotFound
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
    val prisonerId = if (ConsumerFilters.hasPrisonAccess(filters)) getPersonService.getNomisNumber(hmppsId, filters) else Response(null, emptyList())
    val prisonAddresses = prisonerId.data?.nomisNumber?.let { prisonApiGateway.getAddressesForPerson(it).withoutNotFound() } ?: Response(data = emptyList(), errors = prisonerId.errors)

    val probationId = if (ConsumerFilters.hasProbationAccess(filters)) getPersonService.convert(hmppsId, GetPersonService.IdentifierType.CRN) else Response(null, emptyList())
    val probationAddresses = probationId.data?.let { deliusGateway.getAddressesForPerson(it).withoutNotFound() } ?: Response(data = emptyList(), errors = probationId.errors)

    return Response.merge(listOf(prisonAddresses, probationAddresses))
  }
}
