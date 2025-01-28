package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionCreateResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class PostTransactionForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    prisonId: String,
    hmppsId: String,
    transactionRequest: TransactionRequest,
    filters: ConsumerFilters? = null,
  ): Response<TransactionCreateResponse?> {
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess(prisonId, filters)

    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck as Response<TransactionCreateResponse?>
    }

    val personResponse = getPersonService.getNomisNumber(hmppsId = hmppsId)

    if (personResponse == null) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Nomis number not found")),
      )
    }

    val nomisNumber = personResponse.data?.nomisNumber

    if (nomisNumber == null) {
      return Response(
        data = null,
        errors = personResponse.errors,
      )
    }

    val response =
      nomisGateway.postTransactionForPerson(
        prisonId,
        nomisNumber,
        transactionRequest,
      )

    if (response.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = response.errors,
      )
    }

    val transactionCreateResponse = TransactionCreateResponse()
    if (!response.data.id.isNullOrBlank()) {
      transactionCreateResponse.transactionId = response.data.id
    }

    return Response(
      data = transactionCreateResponse,
      errors = emptyList(),
    )
  }
}
