package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AccountBalance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetBalancesForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    prisonId: String,
    hmppsId: String,
    filters: ConsumerFilters? = null,
  ): Response<Balances?> {
    if (
      filters != null && !filters.matchesPrison(prisonId)
    ) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")),
      )
    }

    val personResponse = getPersonService.getNomisNumber(hmppsId = hmppsId)
    val nomisNumber = personResponse.data?.nomisNumber

    if (nomisNumber == null) {
      return Response(
        data = null,
        errors = personResponse.errors,
      )
    }

    val nomisAccounts = nomisGateway.getAccountsForPerson(prisonId, nomisNumber)

    if (nomisAccounts.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = nomisAccounts.errors,
      )
    }

    val nomisSpends = nomisAccounts.data?.spends
    val nomisSavings = nomisAccounts.data?.savings
    val nomisCash = nomisAccounts.data?.cash

    if (nomisSpends == null || nomisSavings == null || nomisCash == null) {
      throw IllegalStateException("Error occurred while trying to get accounts for person with id: $hmppsId")
    }

    val balance =
      Balances(
        balances =
          listOf(
            AccountBalance(accountCode = "spends", amount = nomisSpends),
            AccountBalance(accountCode = "savings", amount = nomisSavings),
            AccountBalance(accountCode = "cash", amount = nomisCash),
          ),
      )

    return Response(
      data = balance,
      errors = emptyList(),
    )
  }

  fun getBalance(
    prisonId: String,
    hmppsId: String,
    accountCode: String,
    filters: ConsumerFilters? = null,
  ): Response<Balances?> {
    if (!listOf("spends", "savings", "cash").any { it == accountCode }) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.NOMIS)),
      )
    }

    val response = execute(prisonId, hmppsId, filters)

    if (response.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = response.errors,
      )
    }

    val accountBalance = response.data?.balances?.filter { it.accountCode == accountCode }?.firstOrNull()

    if (accountBalance == null) {
      throw IllegalStateException("Error occurred while trying to get accounts for person with id: $hmppsId")
    }

    val balance = Balances(balances = listOf(accountBalance))
    return Response(data = balance, errors = emptyList())
  }
}
