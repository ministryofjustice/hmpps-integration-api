package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.CrnSupplier
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [ImageController::class])
@ActiveProfiles("test")
internal class ImageControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getImageService: GetImageService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val crnSupplier: CrnSupplier,
  @MockitoBean val getCaseAccess: GetCaseAccess,
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
          Mockito.reset(auditService)
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised("$basePath/$id")

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("gets a image with the matching ID") {
          mockMvc.performAuthorised("$basePath/$id")

          verify(getImageService, VerificationModeFactory.times(1)).execute(id)
        }

        it("returns an image with the matching ID") {
          val result = mockMvc.performAuthorised("$basePath/$id")

          result.response.contentAsByteArray.shouldBe(image)
        }

        it("logs audit") {
          mockMvc.performAuthorised("$basePath/$id")

          verify(auditService, VerificationModeFactory.times(1)).createEvent("GET_PERSON_IMAGE", mapOf("imageId" to id.toString()))
        }

        it("returns a 404 NOT FOUND status code") {
          whenever(getImageService.execute(id)).thenReturn(
            Response(
              data = byteArrayOf(),
              errors =
                listOf(
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

        it("returns a 500 INTERNAL SERVER ERROR status code when upstream api return expected error") {

          whenever(getImageService.execute(id)).doThrow(
            WebClientResponseException(500, "MockError", null, null, null, null),
          )

          val result = mockMvc.performAuthorised("$basePath/$id")
          assert(result.response.status == 500)
          assert(
            result.response.contentAsString.equals(
              "{\"status\":500,\"errorCode\":null,\"userMessage\":\"500 MockError\",\"developerMessage\":\"Unable to complete request as an upstream service is not responding\",\"moreInfo\":null}",
            ),
          )
        }
      }
    },
  )
