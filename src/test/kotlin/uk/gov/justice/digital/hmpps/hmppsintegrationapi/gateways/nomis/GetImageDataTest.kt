package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class GetImageDataTest(@MockBean val hmppsAuthGateway: HmppsAuthGateway, private val nomisGateway: NomisGateway) :
  DescribeSpec({
    val nomisApiMockServer = NomisApiMockServer()
    val imageId = 5678

    beforeEach {
      nomisApiMockServer.start()
      nomisApiMockServer.stubGetImageData(imageId)

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      nomisApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      nomisGateway.getImageData(imageId)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
    }

    it("returns an image with the matching ID") {
      val expectedImage = File("src/test/resources/__files/example.jpg").readBytes()

      val response = nomisGateway.getImageData(imageId)

      response.data.shouldBe(expectedImage)
    }

    it("returns an error when 404 Not Found is returned") {
      nomisApiMockServer.stubGetImageData(imageId, HttpStatus.NOT_FOUND)

      val response = nomisGateway.getImageData(imageId)

      response.errors.shouldHaveSize(1)
      response.errors.first().causedBy.shouldBe(UpstreamApi.NOMIS)
      response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
    }
  },)
