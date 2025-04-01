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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Transaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Transactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Type
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetTransactionsForPersonService::class],
)
internal class GetTransactionsForPersonServiceTest(
  @MockitoBean val nomisGateway: NomisGateway,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val getTransactionsForPersonService: GetTransactionsForPersonService,
) : DescribeSpec({
    val hmppsId = "1234/56789B"
    val nomisNumber = "Z99999ZZ"
    val prisonId = "ABC"
    val accountCode = "spends"
    val startDate = "2019-04-01"
    val endDate = "2019-04-05"
    val filters = ConsumerFilters(null)
    val exampleTransactions = Transactions(listOf(Transaction("204564839-3", Type(code = "spends", desc = "Spends desc"), "Spends account code", 12345, "2016-10-21", "Client Ref")))

    beforeEach {
      Mockito.reset(getPersonService)
      Mockito.reset(nomisGateway)

      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Transactions>(prisonId, filters)).thenReturn(
        Response(data = null),
      )

      require(hmppsId.matches(Regex("^[0-9]+/[0-9A-Za-z]+$"))) {
        "Invalid Hmpps Id format: $hmppsId"
      }

      whenever(getPersonService.getNomisNumber(hmppsId = hmppsId)).thenReturn(
        Response(data = NomisNumber(nomisNumber = nomisNumber)),
      )

      whenever(
        nomisGateway.getTransactionsForPerson(
          prisonId,
          nomisNumber,
          accountCode,
          startDate,
          endDate,
        ),
      ).thenReturn(
        Response(
          data = exampleTransactions,
        ),
      )
    }

    it("gets a person using a Hmpps ID") {
      getTransactionsForPersonService.execute(
        hmppsId,
        prisonId,
        accountCode,
        startDate,
        endDate,
        filters,
      )

      verify(getPersonService, VerificationModeFactory.times(1)).getNomisNumber(hmppsId = hmppsId)
    }

    it("gets transactions from NOMIS using a prisoner number") {
      getTransactionsForPersonService.execute(
        hmppsId,
        prisonId,
        accountCode,
        startDate,
        endDate,
        filters,
      )

      verify(nomisGateway, VerificationModeFactory.times(1)).getTransactionsForPerson(
        prisonId,
        nomisNumber,
        accountCode,
        startDate,
        endDate,
      )
    }

    it("returns a person's transactions given a Hmpps ID") {
      val result =
        getTransactionsForPersonService.execute(
          hmppsId,
          prisonId,
          accountCode,
          startDate,
          endDate,
          filters,
        )

      result.data.shouldBe(exampleTransactions.toTransactionList())
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
      val response =
        getTransactionsForPersonService.execute(
          hmppsId,
          prisonId,
          accountCode,
          startDate,
          endDate,
          filters,
        )
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
      val response =
        getTransactionsForPersonService.execute(
          hmppsId,
          prisonId,
          accountCode,
          startDate,
          endDate,
          filters,
        )
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.NOMIS,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }

    it("records upstream API errors when getTransactionsForPerson returns errors") {
      whenever(
        nomisGateway.getTransactionsForPerson(
          prisonId,
          nomisNumber,
          accountCode,
          startDate,
          endDate,
        ),
      ).thenReturn(
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
      val response =
        getTransactionsForPersonService.execute(
          hmppsId,
          prisonId,
          accountCode,
          startDate,
          endDate,
          filters,
        )
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.NOMIS,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }

    it("returns null when transactions are requested from an unapproved prison") {
      val consumerFillters = ConsumerFilters(prisons = listOf("ABC"))
      val wrongPrisonId = "XYZ"
      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Transactions>(wrongPrisonId, consumerFillters)).thenReturn(
        Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"))),
      )

      val result =
        getTransactionsForPersonService.execute(
          hmppsId,
          wrongPrisonId,
          accountCode,
          startDate,
          endDate,
          consumerFillters,
        )

      result.data.shouldBe(null)
      result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
    }

    it("returns transactions when requested from an approved prison") {
      val consumerFillters = ConsumerFilters(prisons = listOf("ABC"))
      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Transactions>(prisonId, consumerFillters)).thenReturn(
        Response(data = null),
      )

      val result =
        getTransactionsForPersonService.execute(
          hmppsId,
          prisonId,
          accountCode,
          startDate,
          endDate,
          consumerFillters,
        )

      result.data.shouldBe(exampleTransactions.toTransactionList())
    }
  })
