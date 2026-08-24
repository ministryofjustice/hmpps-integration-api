package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.RESTAPICLIENT_FOR_PRISON_API_GATEWAY
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Transactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Type
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class GetTransactionsForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val restApiClient: RestApiClient,
  val prisonApiGateway: PrisonApiGateway,
) : DescribeSpec(
    {
      val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
      val nomisNumber = "AA1234Z"
      val prisonId = "XYZ"
      val accountCode = "spends"
      val fromDate = "2019-04-01"
      val toDate = "2019-04-05"
      val transactionsPath = "/api/transactions/prison/$prisonId/offenders/$nomisNumber/accounts/$accountCode?from_date=$fromDate&to_date=$toDate"
      val responseJson =
        """
        {
          "transactions": [
            {
              "id": "204564839-3",
              "type": {
                "code": "spends",
                "desc": "Spends account code"
              },
              "description": "Transfer In Regular from caseload PVR",
              "amount": 12345,
              "date": "2016-10-21"
            }
          ]
        }

        """.removeWhitespaceAndNewlines()

      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubForGet(
          transactionsPath,
          responseJson,
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nomisApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        prisonApiGateway.getTransactionsForPerson(
          prisonId,
          nomisNumber,
          accountCode,
          fromDate,
          toDate,
        )

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns transactions for the matching person ID") {
        val response =
          prisonApiGateway.getTransactionsForPerson(
            prisonId,
            nomisNumber,
            accountCode,
            fromDate,
            toDate,
          )

        response.errors.shouldBeEmpty()
        response.data
          ?.transactions
          ?.get(0)
          ?.id
          .shouldBe("204564839-3")
        response.data
          ?.transactions
          ?.get(0)
          ?.type
          ?.code
          .shouldBe("spends")
        response.data
          ?.transactions
          ?.get(0)
          ?.type
          ?.desc
          .shouldBe("Spends account code")
        response.data
          ?.transactions
          ?.get(0)
          ?.description
          .shouldBe("Transfer In Regular from caseload PVR")
        response.data
          ?.transactions
          ?.get(0)
          ?.amount
          .shouldBe(12345)
        response.data
          ?.transactions
          ?.get(0)
          ?.date
          .shouldBe("2016-10-21")
      }

      it("returns an error when 404 Not Found is returned because no person is found") {
        nomisApiMockServer.stubForGet(transactionsPath, "", HttpStatus.NOT_FOUND)

        val response = prisonApiGateway.getAccountsForPerson(prisonId, nomisNumber)

        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.PRISON_API)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }

      it("can use the RestApiClient") {
        // Given
        val authToken = "ABC123"
        val headers = mapOf("Authorization" to "Bearer $authToken")

        val features = FeatureFlagConfig(mapOf(RESTAPICLIENT_FOR_PRISON_API_GATEWAY to true))

        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(transactionsPath), eq(Transactions::class), eq(headers), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.OK,
            RestApiClient.mapResponse(responseJson, Transactions::class),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        // When
        val response =
          gateway.getTransactionsForPerson(
            prisonId,
            nomisNumber,
            accountCode,
            fromDate,
            toDate,
          )

        // Then
        response.errors.shouldBeEmpty()
        response.data
          ?.transactions
          ?.get(0)
          ?.id
          .shouldBe("204564839-3")
        response.data
          ?.transactions
          ?.get(0)
          ?.type
          ?.code
          .shouldBe("spends")
        response.data
          ?.transactions
          ?.get(0)
          ?.type
          ?.desc
          .shouldBe("Spends account code")
        response.data
          ?.transactions
          ?.get(0)
          ?.description
          .shouldBe("Transfer In Regular from caseload PVR")
        response.data
          ?.transactions
          ?.get(0)
          ?.amount
          .shouldBe(12345)
        response.data
          ?.transactions
          ?.get(0)
          ?.date
          .shouldBe("2016-10-21")
      }
    },
  )
