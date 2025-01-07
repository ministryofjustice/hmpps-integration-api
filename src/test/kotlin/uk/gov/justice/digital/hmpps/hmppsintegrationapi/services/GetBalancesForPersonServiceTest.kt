package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AccountBalance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisAccounts

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetBalancesForPersonService::class],
)
internal class GetBalancesForPersonServiceTest(
  @MockitoBean val nomisGateway: NomisGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getBalancesForPersonService: GetBalancesForPersonService,
) : DescribeSpec({

    val hmppsId = "1234/56789B"
    val nomisNumber = "Z99999ZZ"
    val prisonId = "ABC"
    val nomisSpends = 101
    val nomisSavings = 102
    val nomisCash = 103

    beforeEach {
      Mockito.reset(getPersonService)
      Mockito.reset(nomisGateway)

      require(hmppsId.matches(Regex("^[0-9]+/[0-9A-Za-z]+$"))) {
        "Invalid Hmpps Id format: $hmppsId"
      }

      whenever(getPersonService.getNomisNumber(hmppsId = hmppsId)).thenReturn(
        Response(data = NomisNumber(nomisNumber = nomisNumber)),
      )

      whenever(nomisGateway.getAccountsForPerson(prisonId, nomisNumber)).thenReturn(
        Response(
          data = NomisAccounts(spends = nomisSpends, savings = nomisSavings, cash = nomisCash),
        ),
      )
    }
    val balance =
      Balances(
        accountBalances =
          listOf(
            AccountBalance(accountCode = "spends", amount = nomisSpends),
            AccountBalance(accountCode = "saving", amount = nomisSavings),
            AccountBalance(accountCode = "cash", amount = nomisCash),
          ),
      )

    it("gets a person using a Hmpps ID") {
      getBalancesForPersonService.execute(prisonId, hmppsId)

      verify(getPersonService, VerificationModeFactory.times(1)).getNomisNumber(hmppsId = hmppsId)
    }

    it("gets accounts from NOMIS using a prisoner number") {
      getBalancesForPersonService.execute(prisonId, hmppsId)

      verify(nomisGateway, VerificationModeFactory.times(1)).getAccountsForPerson(prisonId, nomisNumber)
    }

    it("returns a person's account balances given a Hmpps ID") {
      val result = getBalancesForPersonService.execute(prisonId, hmppsId)

      result.data.shouldBe(balance)
    }

    describe("when an upstream API returns an error when looking up a balance from a Hmpps ID") {
      beforeEach {
        whenever(getPersonService.getNomisNumber(hmppsId = hmppsId)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )
      }

      it("records upstream API errors") {

        val response = getBalancesForPersonService.execute(prisonId, hmppsId)
        response.errors.shouldHaveSize(1)
      }

//    it("does not call NOMIS if the person is not found") {
//      getBalancesForPersonService.execute(prisonId, hmppsId)
//
//      verify(nomisGateway, VerificationModeFactory.times(0)).getAccountsForPerson(prisonId, nomisNumber)
//    }
    }
  })
