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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionTransferCreateResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionTransferRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.CreditTransaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.DebitTransaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisTransactionTransferResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PostTransactionTransferForPersonService::class],
)
internal class PostTransactionTransferForPersonServiceTest(
  @MockitoBean val nomisGateway: NomisGateway,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val postTransactionTransferForPersonService: PostTransactionTransferForPersonService,
) : DescribeSpec({
    val hmppsId = "1234/56789B"
    val nomisNumber = "Z99999ZZ"
    val prisonId = "ABC"
    val description = "Canteen Purchase of £16.34"
    val amount = 1634
    val clientTransactionId = "CL123212"
    val clientUniqueRef = "CLIENT121131-0_11"
    val filters = ConsumerFilters(null)
    val fromAccount = "spends"
    val toAccount = "savings"
    val exampleTransfer = TransactionTransferRequest(description, amount, clientTransactionId, clientUniqueRef, fromAccount, toAccount)
    val exampleTransferResponse =
      NomisTransactionTransferResponse(
        debitTransaction = DebitTransaction(id = "2345"),
        creditTransaction = CreditTransaction(id = "3456"),
        transactionId = 1234,
      )

    beforeEach {
      Mockito.reset(getPersonService)
      Mockito.reset(nomisGateway)

      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess(prisonId, filters)).thenReturn(
        Response(data = null),
      )

      require(hmppsId.matches(Regex("^[0-9]+/[0-9A-Za-z]+$"))) {
        "Invalid Hmpps Id format: $hmppsId"
      }

      whenever(getPersonService.getNomisNumber(hmppsId = hmppsId)).thenReturn(
        Response(data = NomisNumber(nomisNumber = nomisNumber)),
      )

      whenever(
        nomisGateway.postTransactionTransferForPerson(
          prisonId,
          nomisNumber,
          exampleTransfer,
        ),
      ).thenReturn(
        Response(
          data = exampleTransferResponse,
        ),
      )
    }

    it("post a transfer and verify we get the nomis number prior") {
      postTransactionTransferForPersonService.execute(
        prisonId,
        hmppsId,
        exampleTransfer,
        filters,
      )

      verify(getPersonService, VerificationModeFactory.times(1)).getNomisNumber(hmppsId = hmppsId)
    }

    it("Posts a transfer") {
      postTransactionTransferForPersonService.execute(
        prisonId,
        hmppsId,
        exampleTransfer,
        filters,
      )

      verify(nomisGateway, VerificationModeFactory.times(1)).postTransactionTransferForPerson(
        prisonId,
        nomisNumber,
        exampleTransfer,
      )
    }

    it("posts a transfer and recieves expected response object") {
      val result =
        postTransactionTransferForPersonService.execute(
          prisonId,
          hmppsId,
          exampleTransfer,
          filters,
        )

      result.data.shouldBe(
        TransactionTransferCreateResponse(
          debitTransactionId = exampleTransferResponse.debitTransaction.id,
          creditTransactionId = exampleTransferResponse.creditTransaction.id,
          transactionId = exampleTransferResponse.transactionId.toString(),
        ),
      )
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
        postTransactionTransferForPersonService.execute(
          prisonId,
          hmppsId,
          exampleTransfer,
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
        postTransactionTransferForPersonService.execute(
          prisonId,
          hmppsId,
          exampleTransfer,
          filters,
        )
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.NOMIS,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }
  })
