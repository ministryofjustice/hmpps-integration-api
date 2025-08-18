package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.CACHE_AUTH_TOKEN
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.HmppsAuthFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [(HmppsAuthGateway::class)],
)
class HmppsAuthGatewayTest(
  hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val telemetryService: TelemetryService,
) : DescribeSpec({
    val hmppsAuthMockServer = HmppsAuthMockServer()

    beforeEach {
      hmppsAuthMockServer.start()
      Mockito.reset(telemetryService)
      hmppsAuthMockServer.stubGetOAuthToken("username", "password")
    }

    afterTest {
      hmppsAuthMockServer.stop()
      hmppsAuthGateway.reset()
    }

    it("throws an exception if connection is refused") {
      hmppsAuthMockServer.stop()

      val exception =
        shouldThrow<HmppsAuthFailedException> {
          hmppsAuthGateway.getClientToken("NOMIS")
        }

      exception.message.shouldBe("Connection to localhost:3000 failed for NOMIS.")
    }

    it("throws an exception if auth service is unavailable") {
      hmppsAuthMockServer.stubServiceUnavailableForGetOAuthToken()

      val exception =
        shouldThrow<HmppsAuthFailedException> {
          hmppsAuthGateway.getClientToken("NOMIS")
        }

      exception.message.shouldBe("localhost:3000 is unavailable for NOMIS.")
    }

    it("throws an exception if credentials are invalid") {
      hmppsAuthMockServer.stubUnauthorizedForGetOAAuthToken()

      val exception =
        shouldThrow<HmppsAuthFailedException> {
          hmppsAuthGateway.getClientToken("NOMIS")
        }

      exception.message.shouldBe("Invalid credentials used for NOMIS.")
    }

    it("re-uses the existing access token if it is still valid") {
      whenever(featureFlagConfig.isEnabled(CACHE_AUTH_TOKEN)).thenReturn(true)
      val firstMockedToken = hmppsAuthMockServer.getToken()
      hmppsAuthMockServer.stubGetOAuthToken("client", "client-secret", firstMockedToken)
      val firstToken = hmppsAuthGateway.getClientToken("NOMIS")
      firstToken shouldBe firstMockedToken

      val secondMockedToken = hmppsAuthMockServer.getToken()
      hmppsAuthMockServer.stubGetOAuthToken("client", "client-secret", hmppsAuthMockServer.getToken())
      val secondToken = hmppsAuthGateway.getClientToken("NOMIS")
      secondToken shouldBe firstToken
      secondToken shouldNotBe secondMockedToken

      verify(telemetryService, times(1)).trackEvent("AuthTokenRequest")
      verify(telemetryService, times(1)).trackEvent("AuthTokenCache")
    }

    it("asks for new token if the existing access token is not valid") {
      whenever(featureFlagConfig.isEnabled(CACHE_AUTH_TOKEN)).thenReturn(true)
      val firstMockedToken = hmppsAuthMockServer.getToken(expiresInMinutes = 0)
      hmppsAuthMockServer.stubGetOAuthToken("client", "client-secret", firstMockedToken)
      val firstToken = hmppsAuthGateway.getClientToken("NOMIS")
      firstToken shouldBe firstMockedToken

      val secondMockedToken = hmppsAuthMockServer.getToken()
      hmppsAuthMockServer.stubGetOAuthToken("client", "client-secret", secondMockedToken)
      val secondToken = hmppsAuthGateway.getClientToken("NOMIS")
      secondToken shouldBe secondMockedToken
      secondToken shouldNotBe firstToken

      verify(telemetryService, times(2)).trackEvent("AuthTokenRequest")
    }

    it("does not cache the token when feature flag disabled") {
      whenever(featureFlagConfig.isEnabled(CACHE_AUTH_TOKEN)).thenReturn(false)
      val firstMockedToken = hmppsAuthMockServer.getToken()
      hmppsAuthMockServer.stubGetOAuthToken("client", "client-secret", firstMockedToken)
      val firstToken = hmppsAuthGateway.getClientToken("NOMIS")
      firstToken shouldBe firstMockedToken

      val secondMockedToken = hmppsAuthMockServer.getToken()
      hmppsAuthMockServer.stubGetOAuthToken("client", "client-secret", secondMockedToken)
      val secondToken = hmppsAuthGateway.getClientToken("NOMIS")
      secondToken shouldBe secondMockedToken
      secondToken shouldNotBe firstToken

      verify(telemetryService, times(2)).trackEvent("AuthTokenRequest")
    }
  })
