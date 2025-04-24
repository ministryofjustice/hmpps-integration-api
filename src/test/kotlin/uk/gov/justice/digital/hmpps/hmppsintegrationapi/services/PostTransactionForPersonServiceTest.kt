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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionCreateResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiTransactionResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PostTransactionForPersonService::class],
)
internal class PostTransactionForPersonServiceTest(
  @MockitoBean val prisonApiGateway: PrisonApiGateway,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val postTransactionForPersonService: PostTransactionForPersonService,
) : DescribeSpec({
    val hmppsId = "1234/56789B"
    val nomisNumber = "Z99999ZZ"
    val prisonId = "ABC"
    val type = "CANT"
    val description = "Canteen Purchase of £16.34"
    val amount = 1634
    val clientTransactionId = "CL123212"
    val clientUniqueRef = "CLIENT121131-0_11"
    val filters = ConsumerFilters(null)
    val exampleTransaction = TransactionRequest(type, description, amount, clientTransactionId, clientUniqueRef)
    val exampleTransactionResponse =
      PrisonApiTransactionResponse(
        id = "SUCCESS",
      )

    beforeEach {
      Mockito.reset(getPersonService)
      Mockito.reset(prisonApiGateway)

      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<TransactionCreateResponse>(prisonId, filters)).thenReturn(
        Response(data = null),
      )

      require(hmppsId.matches(Regex("^[0-9]+/[0-9A-Za-z]+$"))) {
        "Invalid Hmpps Id format: $hmppsId"
      }

      whenever(getPersonService.getNomisNumber(hmppsId = hmppsId)).thenReturn(
        Response(data = NomisNumber(nomisNumber = nomisNumber)),
      )

      whenever(
        prisonApiGateway.postTransactionForPerson(
          prisonId,
          nomisNumber,
          exampleTransaction,
        ),
      ).thenReturn(
        Response(
          data = exampleTransactionResponse,
        ),
      )
    }

    it("post a transaction and verify we get the nomis number prior") {
      postTransactionForPersonService.execute(
        prisonId,
        hmppsId,
        exampleTransaction,
        filters,
      )

      verify(getPersonService, VerificationModeFactory.times(1)).getNomisNumber(hmppsId = hmppsId)
    }

    it("Posts a transaction") {
      postTransactionForPersonService.execute(
        prisonId,
        hmppsId,
        exampleTransaction,
        filters,
      )

      verify(prisonApiGateway, VerificationModeFactory.times(1)).postTransactionForPerson(
        prisonId,
        nomisNumber,
        exampleTransaction,
      )
    }

    it("posts a transaction and recieves expected response object") {
      val result =
        postTransactionForPersonService.execute(
          prisonId,
          hmppsId,
          exampleTransaction,
          filters,
        )

      result.data.shouldBe(TransactionCreateResponse(transactionId = exampleTransactionResponse.id))
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
        postTransactionForPersonService.execute(
          prisonId,
          hmppsId,
          exampleTransaction,
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
                causedBy = UpstreamApi.PRISON_API,
              ),
            ),
        ),
      )
      val response =
        postTransactionForPersonService.execute(
          prisonId,
          hmppsId,
          exampleTransaction,
          filters,
        )
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.PRISON_API,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }
  })
