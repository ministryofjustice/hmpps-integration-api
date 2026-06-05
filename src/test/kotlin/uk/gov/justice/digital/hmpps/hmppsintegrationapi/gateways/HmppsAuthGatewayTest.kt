package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.Mockito
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.CacheConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.HmppsAuthFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import kotlin.test.assertEquals

@ActiveProfiles("test")
@Import(CacheConfig::class)
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [(HmppsAuthGateway::class)],
)
class HmppsAuthGatewayTest(
  hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val telemetryService: TelemetryService,
  @MockitoSpyBean val cacheManager: CacheManager,
) : DescribeSpec({
    val hmppsAuthMockServer = HmppsAuthMockServer()

    beforeEach {
      cacheManager.resetCaches()
      hmppsAuthMockServer.start()
      Mockito.reset(telemetryService)
      hmppsAuthMockServer.stubGetOAuthToken("username", "password")
    }

    afterTest {
      hmppsAuthMockServer.stop()
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

    it("throws an exception if connection is refused with webclient wrapper") {
      whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.USE_WEBCLIENT_WRAPPER_FOR_HMPPS_AUTH)).thenReturn(true)
      hmppsAuthMockServer.stop()

      val exception =
        shouldThrow<HmppsAuthFailedException> {
          hmppsAuthGateway.getClientToken("NOMIS")
        }

      exception.message.shouldBe("Connection to localhost:3000 failed for NOMIS.")
    }

    it("throws an exception if auth service is unavailable with webclient wrapper") {
      whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.USE_WEBCLIENT_WRAPPER_FOR_HMPPS_AUTH)).thenReturn(true)
      hmppsAuthMockServer.stubServiceUnavailableForGetOAuthToken()

      val exception =
        shouldThrow<HmppsAuthFailedException> {
          hmppsAuthGateway.getClientToken("NOMIS")
        }

      exception.message.shouldBe("localhost:3000 is unavailable for NOMIS.")
    }

    it("throws an exception if credentials are invalid with webclient wrapper") {
      whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.USE_WEBCLIENT_WRAPPER_FOR_HMPPS_AUTH)).thenReturn(true)
      hmppsAuthMockServer.stubUnauthorizedForGetOAAuthToken()

      val exception =
        shouldThrow<HmppsAuthFailedException> {
          hmppsAuthGateway.getClientToken("NOMIS")
        }

      exception.message.shouldBe("Invalid credentials used for NOMIS.")
    }

    it("re-uses the existing access token if it is still valid with webclient wrapper") {
      whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.USE_WEBCLIENT_WRAPPER_FOR_HMPPS_AUTH)).thenReturn(true)
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

    it("asks for new token if the existing access token is not valid with webclient wrapper") {
      whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.USE_WEBCLIENT_WRAPPER_FOR_HMPPS_AUTH)).thenReturn(true)
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

    it("includes the oboUserName in the URI sent to hmpps auth and caches") {
      whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.USE_WEBCLIENT_WRAPPER_FOR_HMPPS_AUTH)).thenReturn(true)
      val firstMockedToken = hmppsAuthMockServer.getToken()
      hmppsAuthMockServer.stubGetOAuthToken("client", "client-secret", firstMockedToken, "testUser")
      val client = WebClientWrapper("http://localhost:3000")
      val wrapper = spy(client)
      ReflectionTestUtils.setField(hmppsAuthGateway, "webClientWrapper", wrapper)
      val firstToken = hmppsAuthGateway.getClientToken("NOMIS", buildRequestContext(oboUserName = "testUser"))
      val uri = argumentCaptor<String>()
      verify(wrapper, atLeast(1)).getResponseBodySpec(eq(HttpMethod.POST), uri.capture(), anyMap(), eq(null))
      assertEquals("/auth/oauth/token?grant_type=client_credentials&username=testUser", uri.firstValue)
      verify(telemetryService, times(1)).trackEvent("AuthTokenRequest")

      // Second request with obo username should be the same as the first
      val secondToken = hmppsAuthGateway.getClientToken("NOMIS", buildRequestContext(oboUserName = "testUser"))
      secondToken shouldBe firstToken
      verify(telemetryService, times(1)).trackEvent("AuthTokenCache")
    }
  })
