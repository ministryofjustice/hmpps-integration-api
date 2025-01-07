package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetBalancesForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    prisonId: String,
    hmppsId: String,
  ): Response<Map<String, Any>> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val nomisNumber = personResponse.data?.identifiers?.nomisNumber

    val nomisAccounts = nomisGateway.getAccountsForPerson(prisonId, nomisNumber)
    val nomisSpends = nomisAccounts.data?.spends ?: 0
    val nomisSavings = nomisAccounts.data?.savings ?: 0
    val nomisCash = nomisAccounts.data?.cash ?: 0

    val balances = arrayOf(mapOf("accountCode:" to "spends", "amount:" to nomisSpends), mapOf("accountCode:" to "savings", "amount:" to nomisSavings), mapOf("accountCode:" to "cash", "amount:" to nomisCash))

//      Balances(
//      accountBalances = arrayOf(
//        AccountBalance(accountCode = "spends", amount = nomisAccounts.data?.spends ?: 0),
//        AccountBalance(accountCode = "saving", amount = nomisAccounts.data?.savings ?: 0),
//        AccountBalance(accountCode = "cash", amount = nomisAccounts.data?.cash ?: 0)
//      )
//    )

    return Response(
      data =
        mapOf(
          "prisonId" to prisonId,
          "prisonerId" to (nomisNumber ?: ""),
          "balances" to balances,
        ),
    )
  }
}
