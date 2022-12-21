package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebClients
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer

class NomisGatewayTest(@Autowired nomisGateway2: NomisGateway) : DescribeSpec({
  val nomisApiMockServer = NomisApiMockServer()
  val hmppsAuthMockServer = HmppsAuthMockServer()
  val offenderNo = "abc123"

  beforeEach {
    nomisApiMockServer.start()
    hmppsAuthMockServer.start()

    nomisApiMockServer.stubGetOffender(offenderNo)
    hmppsAuthMockServer.stubGetSuccessOAuthToken("client", "client-secret")
  }

  afterTest {
    nomisApiMockServer.stop()
    hmppsAuthMockServer.stop()
  }

  describe("#getPerson") {
    describe("when authentication is unsuccessful") {
      it("throws an exception if connection is refused") {
        //val nomisGateway = NomisGateway()

        hmppsAuthMockServer.stop()

        val exception = shouldThrow<AuthenticationFailedException> {
          nomisGateway2.getPerson(offenderNo)
        }

        exception.message.shouldBe("Connection to localhost:3000 failed for NOMIS.")
      }

      it("throws an exception if auth service is unavailable") {
        val nomisGateway = NomisGateway()

        hmppsAuthMockServer.stubGetConnectExceptionOAuthToken()

        val exception = shouldThrow<AuthenticationFailedException> {
          nomisGateway.getPerson(offenderNo)
        }

        exception.message.shouldBe("localhost:3000 is unavailable for NOMIS.")
      }
    }

    describe("when authenticated is successful") {
      it("returns a person with the matching ID") {
        val nomisGateway = NomisGateway()

        val person = nomisGateway.getPerson(offenderNo)

        person?.firstName.shouldBe("John")
        person?.lastName.shouldBe("Smith")
      }
    }
  }
})
