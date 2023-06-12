package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class NomisGatewayTest(@MockBean val hmppsAuthGateway: HmppsAuthGateway, private val nomisGateway: NomisGateway) :
  DescribeSpec({
    val nomisApiMockServer = NomisApiMockServer()
    val offenderNo = "abc123"

    beforeEach {
      nomisApiMockServer.start()
      Mockito.reset(hmppsAuthGateway)

      whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      nomisApiMockServer.stop()
    }

    describe("#getPerson") {
      beforeTest {
        nomisApiMockServer.stubGetOffender(
          offenderNo,
          """
        {
          "offenderNo": "$offenderNo",
          "firstName": "John",
          "middleName": "Muriel",
          "lastName": "Smith",
          "dateOfBirth": "1970-03-15",
          "aliases": [
            {
              "firstName": "Joey",
              "middleName": "Martin",
              "lastName": "Smiles",
              "dob": "1975-10-12"
            }
          ]
        }
        """,
        )
      }

      it("authenticates using HMPPS Auth with credentials") {
        nomisGateway.getPerson(offenderNo)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns a person with the matching ID") {
        val person = nomisGateway.getPerson(offenderNo)

        person?.firstName.shouldBe("John")
        person?.middleName.shouldBe("Muriel")
        person?.lastName.shouldBe("Smith")
        person?.dateOfBirth.shouldBe(LocalDate.parse("1970-03-15"))
        person?.aliases?.first()?.firstName.shouldBe("Joey")
        person?.aliases?.first()?.middleName.shouldBe("Martin")
        person?.aliases?.first()?.lastName.shouldBe("Smiles")
        person?.aliases?.first()?.dateOfBirth.shouldBe(LocalDate.parse("1975-10-12"))
      }

      it("returns a person without aliases when no aliases are found") {
        nomisApiMockServer.stubGetOffender(
          offenderNo,
          """
          {
            "offenderNo": "$offenderNo",
            "firstName": "John",
            "lastName": "Smith",
            "aliases": []
          }
          """,
        )

        val person = nomisGateway.getPerson(offenderNo)

        person?.aliases.shouldBeEmpty()
      }

      it("returns null when 400 Bad Request is returned") {
        nomisApiMockServer.stubGetOffender(
          offenderNo,
          """
          {
            "developerMessage": "reason for bad request"
          }
          """,
          HttpStatus.BAD_REQUEST,
        )

        val person = nomisGateway.getPerson(offenderNo)

        person?.shouldBeNull()
      }

      it("returns null when 404 Not Found is returned") {
        nomisApiMockServer.stubGetOffender(
          offenderNo,
          """
          {
            "developerMessage": "cannot find person"
          }
          """,
          HttpStatus.NOT_FOUND,
        )

        val person = nomisGateway.getPerson(offenderNo)

        person?.shouldBeNull()
      }
    }

    describe("#getPersonImageMetadata") {
      beforeTest {
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
