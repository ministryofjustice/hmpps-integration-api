package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebClients
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer

class NomisGatewayTest : DescribeSpec({
  val nomisApiMockServer = NomisApiMockServer()
  val hmppsAuthMockServer = HmppsAuthMockServer()
  val offenderNo = "abc123"

  beforeTest {
    nomisApiMockServer.start()
    hmppsAuthMockServer.start()

    nomisApiMockServer.stubGetOffender(offenderNo)
    hmppsAuthMockServer.stubGetOauthToken("client", "client-secret")
  }

  afterTest {
    nomisApiMockServer.stop()
    hmppsAuthMockServer.stop()
  }

  describe("#getPerson") {
    it("returns a person with the matching ID") {
      val webClients = WebClients()
      val prisonApiClient = webClients.prisonApiClient(nomisApiMockServer.baseUrl())
      val hmppsAuthClient = webClients.hmppsAuthClient(hmppsAuthMockServer.baseUrl(), "client", "client-secret")
      val nomisGateway = NomisGateway(prisonApiClient, hmppsAuthClient)

      val person = nomisGateway.getPerson(offenderNo)

      person?.firstName.shouldBe("John")
      person?.lastName.shouldBe("Smith")
    }
  }
})
