package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer

class NomisGatewayTest : DescribeSpec({
  val nomisApiMockServer = NomisApiMockServer()
  val hmppsAuthMockServer = HmppsAuthMockServer()

  beforeTest {
    nomisApiMockServer.start()
    hmppsAuthMockServer.start()
  }

  afterTest {
    nomisApiMockServer.stop()
    hmppsAuthMockServer.stop()
  }

  describe("#getPerson") {
    it("returns a person with the matching ID") {
      val offenderNo = "abc123"
      nomisApiMockServer.stubGetOffender(offenderNo)
      hmppsAuthMockServer.stubGetOauthToken("client", "client-secret")
      val nomisGateway = NomisGateway()

      val person = nomisGateway.getPerson(offenderNo)

      person?.firstName.shouldBe("John")
      person?.lastName.shouldBe("Smith")
    }
  }
})
