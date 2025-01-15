package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.assertions.throwables.shouldThrow
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AccountBalance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisAccounts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

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
    val accountCode = "spends"

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
    val balances =
      Balances(
        balances =
          listOf(
            AccountBalance(accountCode = "spends", amount = nomisSpends),
            AccountBalance(accountCode = "savings", amount = nomisSavings),
            AccountBalance(accountCode = "cash", amount = nomisCash),
          ),
      )

    val singleBalance =
      Balance(
        balance = AccountBalance(accountCode = accountCode, amount = nomisSpends),
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

      result.data.shouldBe(balances)
    }

    it("records upstream API errors") {
      whenever(getPersonService.getNomisNumber(hmppsId = hmppsId)).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
        ),
      )
      val response = getBalancesForPersonService.execute(prisonId, hmppsId)
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
          type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
        ).shouldBe(true)
    }

    it("records upstream API errors when hmppsID is invalid") {
      whenever(getPersonService.getNomisNumber(hmppsId = hmppsId)).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                type = UpstreamApiError.Type.BAD_REQUEST,
                causedBy = UpstreamApi.NOMIS,
              ),
            ),
        ),
      )
      val response = getBalancesForPersonService.execute(prisonId, hmppsId)
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.NOMIS,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }

    it("records upstream API errors when getAccountsForPerson returns errors") {
      whenever(nomisGateway.getAccountsForPerson(prisonId, nomisNumber)).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                type = UpstreamApiError.Type.BAD_REQUEST,
                causedBy = UpstreamApi.NOMIS,
              ),
            ),
        ),
      )
      val response = getBalancesForPersonService.execute(prisonId, hmppsId)
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.NOMIS,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }

    it("records data as null and errors as null when getAccountsForPerson returns null data") {
      whenever(nomisGateway.getAccountsForPerson(prisonId, nomisNumber)).thenReturn(Response(data = null, errors = emptyList()))

      shouldThrow<IllegalStateException> {
        getBalancesForPersonService.execute(prisonId, hmppsId)
      }
    }

    it("returns null when balances are requested from an unapproved prison") {
      val wrongPrisonId = "XYZ"
      val result = getBalancesForPersonService.execute(wrongPrisonId, hmppsId, filters = ConsumerFilters(prisons = listOf("ABC")))

      result.data.shouldBe(null)
      result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
    }

    it("returns balances when requested from an approved prison") {
      val result = getBalancesForPersonService.execute(prisonId, hmppsId, filters = ConsumerFilters(prisons = listOf(prisonId)))

      result.data.shouldBe(balances)
    }

    it("returns a balance when given a valid account code") {
      val result = getBalancesForPersonService.getBalance(prisonId = prisonId, hmppsId = hmppsId, accountCode = accountCode)

      result.data.shouldBe(singleBalance)
    }

    it("returns an error when given an invalid account code") {
      val wrongAccountCode = "invalid_account_code"
      val result = getBalancesForPersonService.getBalance(prisonId = prisonId, hmppsId = hmppsId, accountCode = wrongAccountCode)

      result.data.shouldBe(null)
      result.errors.shouldBe(listOf(UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.NOMIS)))
    }

    it("returns null when balance is requested from an unapproved prison") {
      val wrongPrisonId = "XYZ"
      val result = getBalancesForPersonService.getBalance(wrongPrisonId, hmppsId, accountCode, ConsumerFilters(prisons = listOf("ABC")))

      result.data.shouldBe(null)
      result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
    }
  })
