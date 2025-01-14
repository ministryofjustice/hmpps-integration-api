package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Transactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetTransactionsForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    prisonId: String,
    accountCode: String,
    startDate: String,
    endDate: String,
    filters: ConsumerFilters? = null,
  ): Response<Transactions?> {
    if (
      filters != null && !filters.matchesPrison(prisonId)
    ) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")),
      )
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
      data = nomisTransactions.data,
      errors = emptyList(),
    )
  }
}
