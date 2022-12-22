package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebClients
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.AuthenticationFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Credentials

@ContextConfiguration(initializers = [ConfigDataApplicationContextInitializer::class], classes = [(WebClients::class)])
@ActiveProfiles("test")
class HmppsAuthGatewayTest(@Autowired hmppsAuthClient: WebClient) :
  DescribeSpec({
    val hmppsAuthMockServer = HmppsAuthMockServer()

    beforeEach {
      hmppsAuthMockServer.start()

      hmppsAuthMockServer.stubGetOAuthToken("username", "password")
    }

    afterTest {
      hmppsAuthMockServer.stop()
    }

    it("throws an exception if connection is refused") {
      hmppsAuthMockServer.stop()

      val hmppsAuthGateway = HmppsAuthGateway(hmppsAuthClient)

      val exception = shouldThrow<AuthenticationFailedException> {
        hmppsAuthGateway.authenticate(Credentials("username", "password"))
      }

      exception.message.shouldBe("Connection to localhost:3000 failed for NOMIS.")
    }

    it("throws an exception if auth service is unavailable") {
      hmppsAuthMockServer.stubServiceUnavailableForGetOAuthToken()

      val hmppsAuthGateway = HmppsAuthGateway(hmppsAuthClient)

      val exception = shouldThrow<AuthenticationFailedException> {
        hmppsAuthGateway.authenticate(Credentials("username", "password"))
      }

      exception.message.shouldBe("localhost:3000 is unavailable for NOMIS.")
    }

    it("throws an exception if credentials are invalid") {
      hmppsAuthMockServer.stubUnauthorizedForGetOAAuthToken()

      val hmppsAuthGateway = HmppsAuthGateway(hmppsAuthClient)

      val exception = shouldThrow<AuthenticationFailedException> {
        hmppsAuthGateway.authenticate(Credentials("invalid", "invalid"))
      }

      exception.message.shouldBe("Invalid credentials used for NOMIS.")
    }
  })
