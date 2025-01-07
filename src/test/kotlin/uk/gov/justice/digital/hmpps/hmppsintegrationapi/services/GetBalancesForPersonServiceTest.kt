package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisAccounts

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetBalancesForPersonService::class],
)
internal class GetBalancesForPersonServiceTest(
  @MockitoBean val nomisGateway: NomisGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getBalancesForPersonService: GetBalancesForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "1234/56789B"
      val prisonerId = "Z99999ZZ"
      val prisonId = "ABC"
      val nomisSpends = 100
      val nomisSavings = 100
      val nomisCash = 100

      val personFromPrisonOffenderSearch =
        Person(
          firstName = "Chandler",
          lastName = "ProbationBing",
          identifiers = Identifiers(nomisNumber = prisonerId),
        )

      beforeEach {
        Mockito.reset(getPersonService)
        Mockito.reset(nomisGateway)

        require(hmppsId.matches(Regex("^[0-9]+/[0-9A-Za-z]+$"))) {
          "Invalid Hmpps Id format: $hmppsId"
        }

        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
          Response(
            data = personFromPrisonOffenderSearch,
          ),
        )

        whenever(nomisGateway.getAccountsForPerson(prisonId, prisonerId)).thenReturn(
          Response(
            data =
              NomisAccounts(spends = nomisSpends, savings = nomisSavings, cash = nomisCash),
          ),
        )
      }
//    val expectedNomisAccounts = nomisGateway.getAccountsForPerson(prisonId, prisonerId).data
//    val balance = Balances(
//      accountBalances = arrayOf(
//        AccountBalance(accountCode = "spends", amount = nomisSpends),
//        AccountBalance(accountCode = "saving", amount = nomisSavings),
//        AccountBalance(accountCode = "cash", amount = nomisCash),
//      )
//    )
      val balance = arrayOf(mapOf("accountCode:" to "spends", "amount:" to nomisSpends), mapOf("accountCode:" to "savings", "amount:" to nomisSavings), mapOf("accountCode:" to "cash", "amount:" to nomisCash))

      it("gets a person using a Hmpps ID") {
        getBalancesForPersonService.execute(prisonId, hmppsId)

        verify(getPersonService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
      }

      it("gets accounts from NOMIS using a prisoner number") {
        getBalancesForPersonService.execute(prisonId, hmppsId)

        verify(nomisGateway, VerificationModeFactory.times(1)).getAccountsForPerson(prisonId = prisonId, nomisNumber = prisonerId)
      }

      it("Returns a persons account balances given a hmppsId") {
        val result = getBalancesForPersonService.execute(prisonId, hmppsId)

        result.data.shouldBe(
          mapOf("prisonId" to prisonId, "prisonerId" to prisonerId, "balances" to balance),
        )
      }
    },
  )

//    describe("when an upstream API returns an error when looking up a person from a Hmpps ID") {
//      beforeEach {
//        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
//          Response(
//            data = null,
//            errors =
//              listOf(
//                UpstreamApiError(
//                  causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
//                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
//                ),
//                UpstreamApiError(
//                  causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
//                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
//                ),
//              ),
//          ),
//        )
//      }
//
//      it("records upstream API errors") {
//        val response = getOffencesForPersonService.execute(hmppsId)
//        response.errors.shouldHaveSize(2)
//      }
//
//      it("does not get offences from Nomis") {
//        getOffencesForPersonService.execute(hmppsId)
//        verify(nomisGateway, VerificationModeFactory.times(0)).getOffencesForPerson(id = prisonerNumber)
//      }
//
//      it("does not get offences from nDelius") {
//        getOffencesForPersonService.execute(hmppsId)
//        verify(nDeliusGateway, VerificationModeFactory.times(0)).getOffencesForPerson(id = nDeliusCRN)
//      }
//    }
//
//    it("records errors when it cannot find offences for a person") {
//      whenever(nDeliusGateway.getOffencesForPerson(id = nDeliusCRN)).thenReturn(
//        Response(
//          data = emptyList(),
//          errors =
//            listOf(
//              UpstreamApiError(
//                causedBy = UpstreamApi.NDELIUS,
//                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
//              ),
//            ),
//        ),
//      )
//
//      whenever(nomisGateway.getOffencesForPerson(id = prisonerNumber)).thenReturn(
//        Response(
//          data = emptyList(),
//          errors =
//            listOf(
//              UpstreamApiError(
//                causedBy = UpstreamApi.NOMIS,
//                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
//              ),
//            ),
//        ),
//      )
//
//      val response = getOffencesForPersonService.execute(hmppsId)
//      response.errors.shouldHaveSize(2)
//    }
//  },
// )
