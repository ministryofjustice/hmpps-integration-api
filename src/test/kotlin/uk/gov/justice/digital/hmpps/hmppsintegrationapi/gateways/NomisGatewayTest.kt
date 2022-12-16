package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer

class NomisGatewayTest : DescribeSpec({
  val nomisApiMockServer = NomisApiMockServer()

  beforeTest {
    nomisApiMockServer.start()
  }

  afterTest {
    nomisApiMockServer.stop()
  }

  describe("#getPerson") {
    it("returns a person with the matching ID") {
      val offenderNo = 1
      nomisApiMockServer.stubGetOffender(offenderNo)
      val nomisGateway = NomisGateway()

      val person = nomisGateway.getPerson(offenderNo)

      person?.firstName.shouldBe("John")
      person?.lastName.shouldBe("Smith")
    }
  }
})
