package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class GetOffencesForPersonTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  val nomisGateway: NomisGateway,
) :
  DescribeSpec({
    val nomisApiMockServer = NomisApiMockServer()
    val offenderNo = "zyx987"

    beforeEach {
      nomisApiMockServer.start()
      nomisApiMockServer.stubGetOffencesForPerson(
        offenderNo,
        """
          [
            {
              "bookingId": 9887889,
              "offenceDate": "2000-05-06",
              "offenceRangeDate": "2002-07-08",
              "offenceDescription": "stub_offenceDescription",
              "offenceCode": "BB44444",
              "statuteCode": "CC55",
              "mostSerious": true,
              "primaryResultCode": "stub_primaryResultCode",
              "secondaryResultCode": "stub_secondaryResultCode",
              "primaryResultDescription": "stub_primaryResultDescription",
              "secondaryResultDescription": "stub_secondaryResultDescription",
              "primaryResultConviction": true,
              "secondaryResultConviction": true,
              "courtDate": "2003-12-12",
              "caseId": 99
            }
          ]
        """.removeWhitespaceAndNewlines(),
      )

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      nomisApiMockServer.stop()
    }

    describe("#getOffences") {
      it("authenticates using HMPPS Auth with credentials") {
        nomisGateway.getOffencesForPerson(offenderNo)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns offence history for the matching person ID") {
        val response = nomisGateway.getOffencesForPerson(offenderNo)

        response.data.count().shouldBeGreaterThan(0)
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
    }

    describe("#getImageData") {
      val imageId = 5678

      beforeTest {
        nomisApiMockServer.stubGetImageData(imageId)
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
    }
  },)
