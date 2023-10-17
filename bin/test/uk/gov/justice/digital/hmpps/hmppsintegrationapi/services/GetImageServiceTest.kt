package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetImageService::class],
)
internal class GetImageServiceTest(
  @MockBean val nomisGateway: NomisGateway,
  private val getImageService: GetImageService,
) : DescribeSpec(
  {
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

      whenever(nomisGateway.getImageData(id)).thenReturn(Response(data = image))

      val response = getImageService.execute(id)

      response.data.shouldBe(image)
    }

    it("returns the error from NOMIS when an error occurs") {
      whenever(nomisGateway.getImageData(id)).thenReturn(
        Response(
          data = byteArrayOf(),
          errors = listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NOMIS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
        ),
      )

      val response = getImageService.execute(id)

      response.errors.shouldHaveSize(1)
      response.errors.first().causedBy.shouldBe(UpstreamApi.NOMIS)
      response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
    }
  },
)
