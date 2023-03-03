package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers

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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageService

@WebMvcTest(controllers = [ImageController::class])
internal class ImageControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getImageService: GetImageService,
) : DescribeSpec({
  val id = 2461788
  val image = byteArrayOf(0x48, 101, 108, 108, 111)

  describe("GET /images/{id}") {
    beforeTest {
      Mockito.reset(getImageService)
      whenever(getImageService.execute(id)).thenReturn(image)
    }

    it("responds with a 200 OK status") {
      val result = mockMvc.perform(get("/images/$id")).andReturn()

      result.response.status.shouldBe(HttpStatus.OK.value())
    }

    it("retrieves a image with the matching ID") {
      mockMvc.perform(get("/images/$id")).andReturn()

      verify(getImageService, VerificationModeFactory.times(1)).execute(id)
    }

    it("returns an image with the matching ID") {
      val result = mockMvc.perform(get("/images/$id")).andReturn()

      result.response.contentAsByteArray.shouldBe(image)
    }
  }
})
