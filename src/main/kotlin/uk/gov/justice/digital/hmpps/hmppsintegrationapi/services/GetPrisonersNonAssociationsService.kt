package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NonAssociationsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation.NonAssociations
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetPrisonersNonAssociationsService(
  @Autowired val nonAssociationsGateway: NonAssociationsGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    prisonerNumber: String,
    includeOpen: String? = "true",
    includeClosed: String? = "false",
    filters: ConsumerFilters?,
  ): Response<NonAssociations?> {
    val responseFromNonAssociationsGateway = nonAssociationsGateway.getNonAssociationsForPerson(prisonerNumber, includeOpen, includeClosed)

    if (responseFromNonAssociationsGateway.errors.isNotEmpty()) {
      return Response(data = null, responseFromNonAssociationsGateway.errors)
    }
    // revise to use path parameter instead
    val prisonId = responseFromNonAssociationsGateway.data?.prisonId

    if (prisonId == null) {
      return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NON_ASSOCIATIONS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "No prison associated with prisoner")))
    }

    val unpackedResponse = NonAssociations(responseFromNonAssociationsGateway.data.nonAssociations?.nonAssociations ?: emptyList())
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<NonAssociations?>(prisonId, filters, upstreamServiceType = UpstreamApi.NON_ASSOCIATIONS)

    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    if (unpackedResponse.nonAssociations.isEmpty()) {
      return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NON_ASSOCIATIONS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
    }

    return Response(data = unpackedResponse)
  }
}
