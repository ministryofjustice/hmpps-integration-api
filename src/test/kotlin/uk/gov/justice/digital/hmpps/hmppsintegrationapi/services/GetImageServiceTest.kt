package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetImageService::class],
)
internal class GetImageServiceTest(
  @MockitoBean val prisonApiGateway: PrisonApiGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getImageService: GetImageService,
) : DescribeSpec(
    {
      val id = 12345
      val prisonerNumber = "Z99999ZZ"
      val image = byteArrayOf(1, 2, 3, 4)
      val filters = null
      val imageMetadata =
        listOf(
          ImageMetadata(
            id = id.toLong(),
            active = true,
            captureDateTime = LocalDateTime.now(),
            view = "ANKLE",
            orientation = "FRONT",
            type = "JPEG",
          ),
        )

      describe("getById") {
        beforeEach {
          Mockito.reset(prisonApiGateway)
          whenever(prisonApiGateway.getImageData(id)).thenReturn(Response(data = image))
        }

        it("should return image data for specified image id") {
          val response = getImageService.getById(id)
          verify(prisonApiGateway, times(1)).getImageData(id)
          response.data.shouldBe(image)
        }
      }

      describe("execute") {
        beforeEach {
          Mockito.reset(prisonApiGateway)
          Mockito.reset(getPersonService)

          whenever(getPersonService.getNomisNumberWithFiltering(hmppsId = prisonerNumber, filters = filters)).thenReturn(Response(NomisNumber(prisonerNumber)))
          whenever(prisonApiGateway.getImageMetadataForPerson(prisonerNumber)).thenReturn(Response(data = imageMetadata))

          whenever(prisonApiGateway.getImageData(id)).thenReturn(Response(data = image))
        }

        it("should return image data for a prisoner according to specified image id") {
          getImageService.execute(id, prisonerNumber, filters)
          verify(prisonApiGateway, VerificationModeFactory.times(1)).getImageMetadataForPerson(prisonerNumber)
        }

        it("should return a list of errors if person not found") {
          whenever(getPersonService.getNomisNumberWithFiltering(hmppsId = "notfound", filters = filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISON_API,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )
          val result = getImageService.execute(id, "notfound", filters)
          result.data.shouldBe(null)
          result.errors
            .first()
            .type
            .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
        }

        it("should return a list of errors if images are not found for person") {
          whenever(prisonApiGateway.getImageMetadataForPerson(prisonerNumber)).thenReturn(
            Response(
              data = emptyList(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISON_API,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )
          val result = getImageService.execute(id, prisonerNumber, filters)
          result.data.shouldBe(null)
          result.errors
            .first()
            .type
            .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
        }

        it("should return a list of errors if the image queried isn't found") {
          whenever(prisonApiGateway.getImageMetadataForPerson(prisonerNumber)).thenReturn(
            Response(
              data = imageMetadata,
              errors = emptyList(),
            ),
          )
          val result = getImageService.execute(99999, prisonerNumber, filters)
          result.data.shouldBe(null)
          result.errors
            .first()
            .type
            .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
        }

        it("returns an image") {
          val response = getImageService.execute(id, prisonerNumber, filters)

          response.data.shouldBe(image)
        }

        it("returns the error from NOMIS when an error occurs") {
          whenever(prisonApiGateway.getImageData(id)).thenReturn(
            Response(
              data = byteArrayOf(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISON_API,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val response = getImageService.execute(id, prisonerNumber, filters)

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
      }
    },
  )
