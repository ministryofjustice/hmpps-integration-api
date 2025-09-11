package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionCreateResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class PostTransactionForPersonService(
  @Autowired val prisonApiGateway: PrisonApiGateway,
  @Autowired val getPersonService: GetPersonService,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    prisonId: String,
    hmppsId: String,
    transactionRequest: TransactionRequest,
    filters: ConsumerFilters? = null,
  ): Response<TransactionCreateResponse?> {
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<TransactionCreateResponse>(prisonId, filters)

    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
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
      prisonApiGateway.postTransactionForPerson(
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

    if (response.data != null) {
      return Response(
        data = TransactionCreateResponse(response.data.id),
        errors = emptyList(),
      )
    }

    throw IllegalStateException("No information provided by upstream system")
  }
}
