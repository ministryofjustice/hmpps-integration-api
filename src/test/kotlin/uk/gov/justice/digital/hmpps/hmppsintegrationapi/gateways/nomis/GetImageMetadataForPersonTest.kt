package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
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
import java.time.LocalDateTime

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class GetImageMetadataForPersonTest(@MockBean val hmppsAuthGateway: HmppsAuthGateway, private val nomisGateway: NomisGateway) :
  DescribeSpec({
    val nomisApiMockServer = NomisApiMockServer()
    val offenderNo = "abc123"

    beforeEach {
      nomisApiMockServer.start()
      nomisApiMockServer.stubGetOffenderImageDetails(
        offenderNo,
        """
        [
          {
            "imageId": 24213,
            "active": true,
            "captureDateTime": "2008-08-27T16:35:00",
            "imageView": "FACE",
            "imageOrientation": "FRONT",
            "imageType": "OFF_BKG"
          }
        ]
      """,
      )

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      nomisApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      nomisGateway.getImageMetadataForPerson(offenderNo)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
    }

    it("returns image metadata for the matching person ID") {
      val response = nomisGateway.getImageMetadataForPerson(offenderNo)

      response.data.first().active.shouldBe(true)
      response.data.first().captureDateTime.shouldBe(LocalDateTime.parse("2008-08-27T16:35:00"))
      response.data.first().view.shouldBe("FACE")
      response.data.first().orientation.shouldBe("FRONT")
      response.data.first().type.shouldBe("OFF_BKG")
    }

    it("returns a person without image metadata when no images are found") {
      nomisApiMockServer.stubGetOffenderImageDetails(offenderNo, "[]")

      val response = nomisGateway.getImageMetadataForPerson(offenderNo)

      response.data.shouldBeEmpty()
    }

    it("returns an error when 404 Not Found is returned") {
      nomisApiMockServer.stubGetOffenderImageDetails(offenderNo, "", HttpStatus.NOT_FOUND)

      val response = nomisGateway.getImageMetadataForPerson(offenderNo)

      response.errors.shouldHaveSize(1)
      response.errors.first().causedBy.shouldBe(UpstreamApi.NOMIS)
      response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
    }
  },)
