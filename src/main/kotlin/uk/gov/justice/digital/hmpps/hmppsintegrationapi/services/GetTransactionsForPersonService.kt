package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Transaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetTransactionsForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    hmppsId: String,
    prisonId: String,
    accountCode: String,
    startDate: String,
    endDate: String,
    filters: ConsumerFilters? = null,
  ): Response<List<Transaction>?> {
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<Transaction>>(prisonId, filters)

    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    if (accountCode !in listOf("spends", "savings", "cash")) {
      throw ValidationException("Account code must either be 'spends', 'savings', or 'cash'")
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
        errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val nomisTransactions =
      nomisGateway.getTransactionsForPerson(
        prisonId,
        nomisNumber,
        accountCode,
        startDate,
        endDate,
      )

    if (nomisTransactions.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = nomisTransactions.errors,
      )
    }

    return Response(
      data = nomisTransactions.data?.toTransactionList(),
      errors = emptyList(),
    )
  }
}
