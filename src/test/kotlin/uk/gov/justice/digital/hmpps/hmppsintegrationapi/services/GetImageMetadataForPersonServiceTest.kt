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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetImageMetadataForPersonService::class]
)
internal class GetImageMetadataForPersonServiceTest(
  @MockBean val nomisGateway: NomisGateway,
  private val getImageMetadataForPersonService: GetImageMetadataForPersonService
) : DescribeSpec({
  val id = "abc123"

  beforeEach {
    Mockito.reset(nomisGateway)
  }

  it("retrieves images details from NOMIS") {
    getImageMetadataForPersonService.execute(id)

    verify(nomisGateway, VerificationModeFactory.times(1)).getImageMetadataForPerson(id)
  }

  it("returns metadata for a persons images") {
    val imageMetadataFromNomis = listOf(
      ImageMetadata(
        id = 1,
        captureDate = LocalDate.parse("2023-03-01")
      )
    )

    whenever(nomisGateway.getImageMetadataForPerson(id)).thenReturn(imageMetadataFromNomis)

    val result = getImageMetadataForPersonService.execute(id)

    result.shouldBe(imageMetadataFromNomis)
  }
})
