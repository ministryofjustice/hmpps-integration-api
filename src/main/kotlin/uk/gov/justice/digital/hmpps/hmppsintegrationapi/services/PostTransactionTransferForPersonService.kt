package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionTransferCreateResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionTransferRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class PostTransactionTransferForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    prisonId: String,
    hmppsId: String,
    transactionTransferRequest: TransactionTransferRequest,
    filters: ConsumerFilters? = null,
  ): Response<TransactionTransferCreateResponse?> {
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess(prisonId, filters)

    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck as Response<TransactionTransferCreateResponse?>
    }
    val personResponse = getPersonService.getNomisNumber(hmppsId = hmppsId)
    val nomisNumber = personResponse.data?.nomisNumber

    if (nomisNumber == null) {
      return Response(
        data = null,
        errors = personResponse.errors,
      )
    }

    val response =
      nomisGateway.postTransactionTransferForPerson(
        prisonId,
        nomisNumber,
        transactionTransferRequest,
      )

    if (response.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = response.errors,
      )
    }

    if (response.data != null) {
      return Response(
        data = TransactionTransferCreateResponse(response.data.debitTransaction.id, response.data.creditTransaction.id, response.data.transactionId.toString()),
        errors = emptyList(),
      )
    }

    throw IllegalStateException("No information provided by upstream system")
  }
}
