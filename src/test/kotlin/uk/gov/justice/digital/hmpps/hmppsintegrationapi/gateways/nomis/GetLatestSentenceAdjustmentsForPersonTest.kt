package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiSentenceSummary

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class GetLatestSentenceAdjustmentsForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val restApiClient: RestApiClient,
  private val prisonApiGateway: PrisonApiGateway,
) : DescribeSpec(
    {
      val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
      val offenderNo = "abc123"
      val sentenceSummaryPath = "/api/offenders/$offenderNo/booking/latest/sentence-summary"
      val responseJson = """
          {
            "prisonerNumber": "A1234AA",
            "latestPrisonTerm": {
              "sentenceAdjustments": {
                "additionalDaysAwarded": 12,
                "unlawfullyAtLarge": 10,
                "lawfullyAtLarge": 2,
                "restoredAdditionalDaysAwarded": 0,
                "specialRemission": 11,
                "recallSentenceRemand": 1,
                "recallSentenceTaggedBail": 3,
                "remand": 6,
                "taggedBail": 3,
                "unusedRemand": 6
              }
            }
          }
        """
      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubForGet(
          sentenceSummaryPath,
          responseJson,
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nomisApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        prisonApiGateway.getLatestSentenceAdjustmentsForPerson(offenderNo)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns sentence adjustments for a person with the matching ID") {
        val response = prisonApiGateway.getLatestSentenceAdjustmentsForPerson(offenderNo)

        response.data?.additionalDaysAwarded.shouldBe(12)
        response.data?.unlawfullyAtLarge.shouldBe(10)
        response.data?.lawfullyAtLarge.shouldBe(2)
        response.data?.restoredAdditionalDaysAwarded.shouldBe(0)
        response.data?.specialRemission.shouldBe(11)
        response.data?.recallSentenceRemand.shouldBe(1)
        response.data?.recallSentenceTaggedBail.shouldBe(3)
        response.data?.remand.shouldBe(6)
        response.data?.taggedBail.shouldBe(3)
        response.data?.unusedRemand.shouldBe(6)
      }

      it("returns an error when 404 NOT FOUND is returned") {
        nomisApiMockServer.stubForGet(
          sentenceSummaryPath,
          """
        {
          "developerMessage": "cannot find person"
        }
        """,
          HttpStatus.NOT_FOUND,
        )

        val response = prisonApiGateway.getLatestSentenceAdjustmentsForPerson(offenderNo)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }

      it("can use the RestApiClient") {
        // Given
        val authToken = "ABC123"
        val headers = mapOf("Authorization" to "Bearer $authToken")

        val features = FeatureFlagConfig(mapOf(RESTAPICLIENT_FOR_PRISON_API_GATEWAY to true))

        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(sentenceSummaryPath), eq(PrisonApiSentenceSummary::class), eq(headers), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.OK,
            RestApiClient.mapResponse(responseJson, PrisonApiSentenceSummary::class),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        // When
        val response = gateway.getLatestSentenceAdjustmentsForPerson(offenderNo)

        // Then
        response.data?.additionalDaysAwarded.shouldBe(12)
        response.data?.unlawfullyAtLarge.shouldBe(10)
        response.data?.lawfullyAtLarge.shouldBe(2)
        response.data?.restoredAdditionalDaysAwarded.shouldBe(0)
        response.data?.specialRemission.shouldBe(11)
        response.data?.recallSentenceRemand.shouldBe(1)
        response.data?.recallSentenceTaggedBail.shouldBe(3)
        response.data?.remand.shouldBe(6)
        response.data?.taggedBail.shouldBe(3)
        response.data?.unusedRemand.shouldBe(6)
      }
    },
  )
