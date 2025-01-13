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
    accountCode: String? = null,
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

    if (accountCode != null) {
      return getBalance(accountCode, nomisSpends, nomisSavings, nomisCash)
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

  private fun getBalance(
    accountCode: String,
    nomisSpends: Int,
    nomisSavings: Int,
    nomisCash: Int,
  ): Response<Balances?> {
    val accountBalance =
      when (accountCode) {
        "spends" -> AccountBalance("spends", nomisSpends)
        "savings" -> AccountBalance("savings", nomisSavings)
        "cash" -> AccountBalance("cash", nomisCash)
        else -> return Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.BAD_REQUEST)))
      }

    val balance = Balances(balances = listOf(AccountBalance(accountCode = accountBalance.accountCode, amount = accountBalance.amount)))
    return Response(data = balance, errors = emptyList())
  }
}
