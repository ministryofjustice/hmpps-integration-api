package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PerformanceTestGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AccountBalance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService.IdentifierType

@Service
class PerformanceTestService(
  @Autowired val performanceTestGateway: PerformanceTestGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    prisonId: String,
    hmppsId: String,
    filters: ConsumerFilters? = null,
  ): Response<Balances?> {
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<Balances>("EXAMPLE", filters)

    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    val personResponse = getNomisNumber(hmppsId = hmppsId)
    val nomisNumber = personResponse.data?.nomisNumber

    if (nomisNumber == null) {
      return Response(
        data = null,
        errors = personResponse.errors,
      )
    }

    val nomisAccounts = performanceTestGateway.getAccountsForPerson(prisonId, nomisNumber)

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
      throw IllegalStateException()
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

  fun getNomisNumber(hmppsId: String): Response<NomisNumber?> =
    when (identifyHmppsId(hmppsId)) {
      IdentifierType.NOMS -> {
        val prisoner = performanceTestGateway.getPrisonOffender(hmppsId)
        if (prisoner.errors.isNotEmpty()) {
          Response(data = null, errors = prisoner.errors)
        } else {
          Response(data = NomisNumber(hmppsId))
        }
      }
      IdentifierType.CRN -> Response(data = null, errors = emptyList())
      IdentifierType.UNKNOWN -> Response(data = null, errors = emptyList())
    }

  fun identifyHmppsId(input: String): IdentifierType {
    val nomsPattern = Regex("^[A-Z]\\d{4}[A-Z]{2}$")
    val crnPattern = Regex("^[A-Z]{1,2}\\d{6}$")

    return when {
      nomsPattern.matches(input) -> IdentifierType.NOMS
      crnPattern.matches(input) -> IdentifierType.CRN
      else -> IdentifierType.UNKNOWN
    }
  }
}
