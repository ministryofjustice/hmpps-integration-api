package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetImageService::class]
)
internal class GetImageServiceTest(
  @MockBean val nomisGateway: NomisGateway,
  private val getImageService: GetImageService
) : DescribeSpec({
  val id = 12345

  beforeEach {
    Mockito.reset(nomisGateway)
  }

  it("retrieves an image from NOMIS") {
    getImageService.execute(id)

    verify(nomisGateway, VerificationModeFactory.times(1)).getImageData(id)
  }

  it("returns an image") {
    val image = byteArrayOf(1, 2, 3, 4)

    whenever(nomisGateway.getImageData(id)).thenReturn(image)

    val result = getImageService.execute(id)

    result.shouldBe(image)
  }
})
