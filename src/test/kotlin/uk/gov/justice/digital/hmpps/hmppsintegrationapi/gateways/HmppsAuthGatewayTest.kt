package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.AuthenticationFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Credentials

@ContextConfiguration(initializers = [ConfigDataApplicationContextInitializer::class], classes = [(HmppsAuthGateway::class)])
@ActiveProfiles("test")
class HmppsAuthGatewayTest(hmppsAuthGateway: HmppsAuthGateway) :
  DescribeSpec({
    val hmppsAuthMockServer = HmppsAuthMockServer()

    // Executes before each test
    beforeEach {
      hmppsAuthMockServer.start()

      hmppsAuthMockServer.stubGetOAuthToken("username", "password")
    }

    // Executes after each test
    afterTest {
      hmppsAuthMockServer.stop()
    }

    it("throws an exception if connection is refused") {
      hmppsAuthMockServer.stop()

      val exception = shouldThrow<AuthenticationFailedException> {
        hmppsAuthGateway.authenticate(Credentials("username", "password"))
      }

      exception.message.shouldBe("Connection to localhost:3000 failed for NOMIS.")
    }

    it("throws an exception if auth service is unavailable") {
      hmppsAuthMockServer.stubServiceUnavailableForGetOAuthToken()

      val exception = shouldThrow<AuthenticationFailedException> {
        hmppsAuthGateway.authenticate(Credentials("username", "password"))
      }

      exception.message.shouldBe("localhost:3000 is unavailable for NOMIS.")
    }

    it("throws an exception if credentials are invalid") {
      hmppsAuthMockServer.stubUnauthorizedForGetOAAuthToken()

      val exception = shouldThrow<AuthenticationFailedException> {
        hmppsAuthGateway.authenticate(Credentials("invalid", "invalid"))
      }

      exception.message.shouldBe("Invalid credentials used for NOMIS.")
    }
  })
