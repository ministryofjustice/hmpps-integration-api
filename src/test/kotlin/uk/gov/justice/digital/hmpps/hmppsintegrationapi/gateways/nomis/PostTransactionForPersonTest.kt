package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class PostTransactionForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  val nomisGateway: NomisGateway,
) : DescribeSpec({
    val nomisApiMockServer = NomisApiMockServer()
    val prisonId = "XYZ"
    val nomisNumber = "AA1234Z"
    val description = "Canteen Purchase of £16.34"
    val amount = 1634
    val clientTransactionId = "CL123212"
    val clientUniqueRef = "CLIENT121131-0_11"
    val type = "CANT"
    val exampleTransaction = TransactionRequest(type, description, amount, clientTransactionId, clientUniqueRef)

    beforeEach {
      nomisApiMockServer.start()

      nomisApiMockServer.stubNomisApiResponseForPost(
        prisonId,
        nomisNumber,
        asJsonString(exampleTransaction.toApiConformingMap()),
        """
        {
          "id": "6179604-1",
          "description": "Transfer In Regular from caseload PVR"
        }
        """.removeWhitespaceAndNewlines(),
      )

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      nomisApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      nomisGateway.postTransactionForPerson(
        prisonId,
        nomisNumber,
        exampleTransaction,
      )

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
    }

    it("returns expected response with transaction id and description when a valid request body is provided") {
      val response =
        nomisGateway.postTransactionForPerson(
          prisonId,
          nomisNumber,
          exampleTransaction,
        )

      response.errors.shouldBeEmpty()
      response.data
        ?.id
        .shouldBe("6179604-1")
      response.data
        ?.description
        .shouldBe("Transfer In Regular from caseload PVR")
    }

    it("return a 500 error response") {
      var invalidTransactionRequest = TransactionRequest("invalid", "", 0, "", "")
      nomisApiMockServer.stubNomisApiResponseForPost(prisonId, nomisNumber, asJsonString(invalidTransactionRequest), "", HttpStatus.NOT_FOUND)

      val response = nomisGateway.postTransactionForPerson(prisonId, nomisNumber, invalidTransactionRequest)

      response.errors.shouldBe(arrayOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)))
    }
  })

private fun asJsonString(obj: Any): String = jacksonObjectMapper().writeValueAsString(obj)
