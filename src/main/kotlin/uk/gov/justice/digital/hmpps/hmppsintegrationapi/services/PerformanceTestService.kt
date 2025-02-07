package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PerformanceTestGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AccountBalance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionCreateResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
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

  fun search(
    firstName: String?,
    lastName: String?,
    dateOfBirth: String?,
    searchWithinAliases: Boolean = false,
    filters: ConsumerFilters?,
  ): Response<List<PersonInPrison>> {
    val prisonIds = filters?.prisons
    val responseFromPrisonerOffenderSearch =
      if (prisonIds == null) {
        // Hit global-search endpoint
        performanceTestGateway.getPersons(
          firstName,
          lastName,
          dateOfBirth,
          searchWithinAliases,
        )
      } else if (prisonIds.isEmpty()) {
        return Response(emptyList(), listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.FORBIDDEN, "Consumer configured with no access to any prisons")))
      } else {
        // Hit prisoner-details endpoint
        performanceTestGateway.getPrisonerDetails(firstName, lastName, dateOfBirth, searchWithinAliases, prisonIds)
      }

    if (responseFromPrisonerOffenderSearch.errors.isNotEmpty()) {
      return Response(emptyList(), responseFromPrisonerOffenderSearch.errors)
    }

    return Response(data = responseFromPrisonerOffenderSearch.data.map { it.toPersonInPrison() })
  }

  fun post(
    prisonId: String,
    hmppsId: String,
    transactionRequest: TransactionRequest,
    filters: ConsumerFilters? = null,
  ): Response<TransactionCreateResponse?> {
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<TransactionCreateResponse>(prisonId, filters)

    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    val personResponse = getNomisNumber(hmppsId)
    val nomisNumber = personResponse.data?.nomisNumber

    if (nomisNumber == null) {
      return Response(
        data = null,
        errors = personResponse.errors,
      )
    }

    val response =
      performanceTestGateway.postTransactionForPerson(
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
