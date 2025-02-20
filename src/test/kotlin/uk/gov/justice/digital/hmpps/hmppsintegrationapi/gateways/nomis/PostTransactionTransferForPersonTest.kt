package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionTransferRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class PostTransactionTransferForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  val nomisGateway: NomisGateway,
) : DescribeSpec({
    val nomisApiMockServer = NomisApiMockServer()
    val prisonId = "XYZ"
    val nomisNumber = "AA1234Z"
    val path = "/api/finance/prison/$prisonId/offenders/$nomisNumber/transfer-to-savings"
    val description = "Canteen Purchase of £16.34"
    val amount = 1634
    val clientTransactionId = "CL123212"
    val clientUniqueRef = "CLIENT121131-0_11"
    val fromAccount = "spends"
    val toAccount = "savings"
    val exampleTransfer = TransactionTransferRequest(description, amount, clientTransactionId, clientUniqueRef, fromAccount, toAccount)

    beforeEach {
      nomisApiMockServer.start()
      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      nomisApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      nomisApiMockServer.stubNomisApiResponseForPost(
        path,
        asJsonString(exampleTransfer.toApiConformingMap()),
        """
        {
            "debitTransaction": {
              "id": "6179604-1"
            },
            "creditTransaction": {
              "id": "6179604-1"
            },
            "transactionId": 6179604
          }
        """.removeWhitespaceAndNewlines(),
      )

      nomisGateway.postTransactionTransferForPerson(
        prisonId,
        nomisNumber,
        exampleTransfer,
      )

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
    }

    it("returns expected response with transaction id and debit and credit transactions when a valid request body is provided") {
      nomisApiMockServer.stubNomisApiResponseForPost(
        path,
        asJsonString(exampleTransfer.toApiConformingMap()),
        """
          {
            "debitTransaction": {
              "id": "6179604-1"
            },
            "creditTransaction": {
              "id": "6179604-1"
            },
            "transactionId": 6179604
          }
        """.removeWhitespaceAndNewlines(),
      )

      val response =
        nomisGateway.postTransactionTransferForPerson(
          prisonId,
          nomisNumber,
          exampleTransfer,
        )

      response.errors.shouldBeEmpty()
      response.data.shouldNotBeNull()
      response.data!!
        .transactionId
        .shouldBe(6179604)
      response.data!!
        .debitTransaction.id
        .shouldBe("6179604-1")
      response.data!!
        .creditTransaction.id
        .shouldBe("6179604-1")
    }

    it("return a 404 error response") {
      val invalidTransferRequest = TransactionTransferRequest("", 0, "", "", "", "")
      nomisApiMockServer.stubNomisApiResponseForPost(
        path,
        asJsonString(invalidTransferRequest),
        "",
        HttpStatus.NOT_FOUND,
      )

      val response = nomisGateway.postTransactionTransferForPerson(prisonId, nomisNumber, invalidTransferRequest)

      response.errors.shouldBe(
        arrayOf(
          UpstreamApiError(
            causedBy = UpstreamApi.NOMIS,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          ),
        ),
      )
    }

    it("return a 409 error response") {
      nomisApiMockServer.stubNomisApiResponseForPost(
        path,
        asJsonString(exampleTransfer.toApiConformingMap()),
        """
        {
        }
        """.removeWhitespaceAndNewlines(),
        HttpStatus.CONFLICT,
      )

      val response = nomisGateway.postTransactionTransferForPerson(prisonId, nomisNumber, exampleTransfer)

      response.errors.shouldBe(
        arrayOf(
          UpstreamApiError(
            causedBy = UpstreamApi.NOMIS,
            type = UpstreamApiError.Type.CONFLICT,
          ),
        ),
      )
    }
  })

private fun asJsonString(obj: Any): String = jacksonObjectMapper().writeValueAsString(obj)
