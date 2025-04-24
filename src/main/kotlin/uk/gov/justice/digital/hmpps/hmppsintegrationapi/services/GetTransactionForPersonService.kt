package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Transaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetTransactionForPersonService(
  @Autowired val prisonApiGateway: PrisonApiGateway,
  @Autowired val getPersonService: GetPersonService,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    hmppsId: String,
    prisonId: String,
    clientUniqueRef: String,
    filters: ConsumerFilters? = null,
  ): Response<Transaction?> {
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<Transaction>(prisonId, filters)

    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    val personResponse = getPersonService.getNomisNumber(hmppsId = hmppsId)

    if (personResponse.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = personResponse.errors,
      )
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")),
      )

    val nomisTransaction =
      prisonApiGateway.getTransactionForPerson(
        prisonId,
        nomisNumber,
        clientUniqueRef,
      )

    if (nomisTransaction.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = nomisTransaction.errors,
      )
    }

    return Response(
      data = nomisTransaction.data,
      errors = emptyList(),
    )
  }
}
