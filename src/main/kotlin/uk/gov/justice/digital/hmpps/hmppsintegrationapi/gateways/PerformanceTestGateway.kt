package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisAccounts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisTransactionResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import kotlin.time.Duration.Companion.milliseconds

@Component
class PerformanceTestGateway {
  fun getAccountsForPerson(
    prisonId: String,
    nomisNumber: String?,
  ): Response<NomisAccounts?> = Response(NomisAccounts(spends = 123, savings = 456, cash = 789))

  fun addDelay(ms: Int = 100) {
    runBlocking {
      // Creates a coroutine that blocks the main thread
      withContext(Dispatchers.IO) {
        delay(ms.milliseconds) // Simulate API latency
      }
    }
  }

  fun getPrisonOffender(nomsNumber: String): Response<POSPrisoner?> {
    addDelay()
    return Response(
      POSPrisoner(
        prisonerNumber = "A1234AA",
        firstName = "EXAMPLE",
        lastName = "PERSON",
      ),
    )
  }

  fun getPersons(
    firstName: String?,
    lastName: String?,
    dateOfBirth: String?,
    searchWithinAliases: Boolean = false,
  ): Response<List<POSPrisoner>> {
    addDelay()
    return Response(
      listOf(
        POSPrisoner(
          prisonerNumber = "A1234AA",
          firstName = "EXAMPLE",
          lastName = "PERSON",
        ),
        POSPrisoner(
          prisonerNumber = "B5678BB",
          firstName = "ANOTHER",
          lastName = "PERSON",
        ),
      ),
    )
  }

  fun getPrisonerDetails(
    firstName: String?,
    lastName: String?,
    dateOfBirth: String?,
    searchWithinAliases: Boolean = false,
    prisonIds: List<String?>?,
  ): Response<List<POSPrisoner>> {
    addDelay()
    return Response(
      listOf(
        POSPrisoner(
          prisonerNumber = "A1234AA",
          firstName = "EXAMPLE",
          lastName = "PERSON",
        ),
        POSPrisoner(
          prisonerNumber = "B5678BB",
          firstName = "ANOTHER",
          lastName = "PERSON",
        ),
      ),
    )
  }

  fun postTransactionForPerson(
    prisonId: String,
    nomisNumber: String,
    transactionRequest: TransactionRequest,
  ): Response<NomisTransactionResponse?> {
    addDelay()
    return Response(data = NomisTransactionResponse(id = "123456-1"))
  }
}
