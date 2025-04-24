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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class PostTransactionForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  val prisonApiGateway: PrisonApiGateway,
) : DescribeSpec({
    val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
    val prisonId = "XYZ"
    val nomisNumber = "AA1234Z"
    val path = "/api/v1/prison/$prisonId/offenders/$nomisNumber/transactions"
    val description = "Canteen Purchase of £16.34"
    val amount = 1634
    val clientTransactionId = "CL123212"
    val clientUniqueRef = "CLIENT121131-0_11"
    val type = "CANT"
    val exampleTransaction = TransactionRequest(type, description, amount, clientTransactionId, clientUniqueRef)

    beforeEach {
      nomisApiMockServer.start()
      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      nomisApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      nomisApiMockServer.stubForPost(
        path,
        asJsonString(exampleTransaction.toApiConformingMap()),
        """
        {
          "id": "6179604-1"
        }
        """.removeWhitespaceAndNewlines(),
      )

      prisonApiGateway.postTransactionForPerson(
        prisonId,
        nomisNumber,
        exampleTransaction,
      )

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
    }

    it("returns expected response with transaction id and description when a valid request body is provided") {
      nomisApiMockServer.stubForPost(
        path,
        asJsonString(exampleTransaction.toApiConformingMap()),
        """
        {
          "id": "6179604-1"
        }
        """.removeWhitespaceAndNewlines(),
      )

      val response =
        prisonApiGateway.postTransactionForPerson(
          prisonId,
          nomisNumber,
          exampleTransaction,
        )

      response.errors.shouldBeEmpty()
      response.data.shouldNotBeNull()
      response.data!!
        .id
        .shouldBe("6179604-1")
    }

    it("return a 400 error response") {
      val invalidTransactionRequest = TransactionRequest("invalid", "", 0, "", "")
      nomisApiMockServer.stubForPost(path, asJsonString(invalidTransactionRequest.toApiConformingMap()), "", HttpStatus.BAD_REQUEST)

      val response = prisonApiGateway.postTransactionForPerson(prisonId, nomisNumber, invalidTransactionRequest)

      response.errors.shouldBe(arrayOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.BAD_REQUEST)))
    }

    it("return a 404 error response") {
      val invalidTransactionRequest = TransactionRequest("invalid", "", 0, "", "")
      nomisApiMockServer.stubForPost(path, asJsonString(invalidTransactionRequest.toApiConformingMap()), "", HttpStatus.NOT_FOUND)

      val response = prisonApiGateway.postTransactionForPerson(prisonId, nomisNumber, invalidTransactionRequest)

      response.errors.shouldBe(arrayOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)))
    }

    it("return a 409 error response") {
      nomisApiMockServer.stubForPost(
        path,
        asJsonString(exampleTransaction.toApiConformingMap()),
        """
        {
        }
        """.removeWhitespaceAndNewlines(),
        HttpStatus.CONFLICT,
      )

      val response = prisonApiGateway.postTransactionForPerson(prisonId, nomisNumber, exampleTransaction)

      response.errors.shouldBe(arrayOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.CONFLICT)))
    }
  })

private fun asJsonString(obj: Any): String = jacksonObjectMapper().writeValueAsString(obj)
