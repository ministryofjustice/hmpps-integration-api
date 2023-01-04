package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer

@ContextConfiguration(initializers = [ConfigDataApplicationContextInitializer::class], classes = [NomisGateway::class, HmppsAuthGateway::class])
@ActiveProfiles("test")
class NomisGatewayTest(@MockBean val hmppsAuthGateway: HmppsAuthGateway, private val nomisGateway: NomisGateway) :
  DescribeSpec({
    val nomisApiMockServer = NomisApiMockServer()
    val offenderNo = "abc123"

    beforeEach {
      nomisApiMockServer.start()
      nomisApiMockServer.stubGetOffender(offenderNo)

      Mockito.`when`(hmppsAuthGateway.getClientToken(any())).thenReturn(
        HmppsAuthMockServer.TOKEN
      )
    }

    afterTest {
      nomisApiMockServer.stop()
    }

    describe("#getPerson") {
      it("authenticates using HMPPS Auth with credentials") {
        nomisGateway.getPerson(offenderNo)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken(any())
      }

      it("returns a person with the matching ID") {
        val person = nomisGateway.getPerson(offenderNo)

        person?.firstName.shouldBe("John")
        person?.lastName.shouldBe("Smith")
        person?.aliases?.first()?.firstName.shouldBe("Joey")
        person?.aliases?.first()?.lastName.shouldBe("Smiles")
      }
    }
  })
