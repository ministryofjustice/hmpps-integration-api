package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AccountBalance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisAccounts

@Service
class GetBalancesForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    prisonId: String,
    hmppsId: String,
  ): Response<Balances> {
    val personResponse = getPersonService.getNomisNumber(hmppsId = hmppsId)
    val nomisNumber = personResponse.data?.nomisNumber
    var nomisAccounts: Response<NomisAccounts?> = Response(data = null)

    if (nomisNumber != null) {
      nomisAccounts = nomisGateway.getAccountsForPerson(prisonId, nomisNumber)
    }

    val nomisSpends = nomisAccounts.data?.spends
    val nomisSavings = nomisAccounts.data?.savings
    val nomisCash = nomisAccounts.data?.cash

    val balance =
      Balances(
        accountBalances =
          listOf(
            AccountBalance(accountCode = "spends", amount = nomisSpends!!),
            AccountBalance(accountCode = "saving", amount = nomisSavings!!),
            AccountBalance(accountCode = "cash", amount = nomisCash!!),
          ),
      )

    return Response(
      data = balance,
      errors = personResponse.errors,
    )
  }
}
