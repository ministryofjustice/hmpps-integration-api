package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageService

@WebMvcTest(controllers = [ImageController::class])
@ActiveProfiles("test")
internal class ImageControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockBean val getImageService: GetImageService,
) : DescribeSpec(
  {
    val id = 2461788
    val image = byteArrayOf(0x48, 101, 108, 108, 111)
    val basePath = "/v1/images"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    describe("GET $basePath/{id}") {
      beforeTest {
        Mockito.reset(getImageService)
        whenever(getImageService.execute(id)).thenReturn(Response(data = image))
      }

      it("responds with a 200 OK status") {
        val result = mockMvc.performAuthorised("$basePath/$id")

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("retrieves a image with the matching ID") {
        mockMvc.performAuthorised("$basePath/$id")

        verify(getImageService, VerificationModeFactory.times(1)).execute(id)
      }

      it("returns an image with the matching ID") {
        val result = mockMvc.performAuthorised("$basePath/$id")

        result.response.contentAsByteArray.shouldBe(image)
      }

      it("responds with a 404 NOT FOUND status") {
        whenever(getImageService.execute(id)).thenReturn(
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

        val result = mockMvc.performAuthorised("$basePath/$id")

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }
    }
  },
)
