package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisAccounts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner

@Component
class PerformanceTestGateway {
  fun getAccountsForPerson(
    prisonId: String,
    nomisNumber: String?,
  ): Response<NomisAccounts?> = Response(NomisAccounts(spends = 123, savings = 456, cash = 789))

  fun getPrisonOffender(nomsNumber: String): Response<POSPrisoner?> =
    Response(
      POSPrisoner(
        prisonerNumber = "A1234AA",
        firstName = "EXAMPLE",
        lastName = "PERSON",
      ),
    )
}
