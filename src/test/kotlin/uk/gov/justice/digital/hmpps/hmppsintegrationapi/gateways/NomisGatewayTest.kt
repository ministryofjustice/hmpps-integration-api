package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebClients
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer

@ContextConfiguration(initializers = [ConfigDataApplicationContextInitializer::class], classes = [(WebClients::class)])
@ActiveProfiles("test")
class NomisGatewayTest(@Autowired prisonApiClient: WebClient, @MockBean val hmppsAuthGateway: HmppsAuthGateway) :
  DescribeSpec({
    val nomisApiMockServer = NomisApiMockServer()
    val offenderNo = "abc123"

    beforeEach {
      nomisApiMockServer.start()
      nomisApiMockServer.stubGetOffender(offenderNo)

      Mockito.`when`(hmppsAuthGateway.authenticate(any())).thenReturn(
        HmppsAuthMockServer.TOKEN
      )
    }

    afterTest {
      nomisApiMockServer.stop()
    }

    describe("#getPerson") {
      it("authenticates using HMPPS Auth with credentials") {
        val nomisGateway = NomisGateway(prisonApiClient, hmppsAuthGateway)

        nomisGateway.getPerson(offenderNo)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).authenticate(any())
      }

      it("returns a person with the matching ID") {
        val nomisGateway = NomisGateway(prisonApiClient, hmppsAuthGateway)

        val person = nomisGateway.getPerson(offenderNo)

        person?.firstName.shouldBe("John")
        person?.lastName.shouldBe("Smith")
      }
    }
  })
