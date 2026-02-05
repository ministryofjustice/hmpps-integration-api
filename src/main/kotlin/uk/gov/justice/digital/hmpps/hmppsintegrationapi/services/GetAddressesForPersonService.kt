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
    val prisonerAddresses =
      if (ConsumerFilters.hasPrisonAccess(filters)) {
        val prisonerId = getPersonService.getNomisNumber(hmppsId, filters)
        prisonerId.data?.nomisNumber?.let { prisonApiGateway.getAddressesForPerson(it).withoutNotFound() } ?: Response(data = emptyList(), errors = prisonerId.errors)
      } else {
        Response(emptyList(), emptyList())
      }

    val probationAddresses =
      if (ConsumerFilters.hasProbationAccess(filters)) {
        val probationId = getPersonService.convert(hmppsId, GetPersonService.IdentifierType.CRN)
        probationId.data?.let { deliusGateway.getAddressesForPerson(it).withoutNotFound() } ?: Response(data = emptyList(), errors = probationId.errors)
      } else {
        Response(emptyList(), emptyList())
      }

    return Response.merge(listOf(prisonerAddresses, probationAddresses))
  }
}
