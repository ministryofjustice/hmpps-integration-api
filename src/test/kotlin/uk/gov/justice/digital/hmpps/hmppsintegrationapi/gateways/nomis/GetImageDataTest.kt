package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class GetImageDataTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val prisonApiGateway: PrisonApiGateway,
) : DescribeSpec({
    val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
    val imageId = 5678

    beforeEach {
      nomisApiMockServer.start()
      nomisApiMockServer.stubForImageData(imageId)

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      nomisApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      prisonApiGateway.getImageData(imageId)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
    }

    it("returns an image with the matching ID") {
      val expectedImage = File("src/test/resources/__files/example.jpg").readBytes()

      val response = prisonApiGateway.getImageData(imageId)

      response.data.shouldBe(expectedImage)
    }

    it("returns an error when 404 Not Found is returned") {
      nomisApiMockServer.stubForImageData(imageId, HttpStatus.NOT_FOUND)

      val response = prisonApiGateway.getImageData(imageId)

      response.errors.shouldHaveSize(1)
      response.errors
        .first()
        .causedBy
        .shouldBe(UpstreamApi.PRISON_API)
      response.errors
        .first()
        .type
        .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
    }
  })
