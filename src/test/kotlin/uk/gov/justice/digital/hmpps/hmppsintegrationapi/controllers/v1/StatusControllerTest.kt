package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.servlet.http.HttpServletRequest
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.RedactionService

@WebMvcTest(controllers = [StatusController::class])
@ActiveProfiles("test")
class StatusControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @Autowired val request: HttpServletRequest,
  @MockitoBean val redactionService: RedactionService,
) : DescribeSpec(
    {
      val path = "/v1/status"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      beforeTest {
        doAnswer { invocation ->
          invocation.arguments[0]
        }.whenever(redactionService).applyPolicies(any(), any())
      }

      describe("GET Status") {

        it("should return 200 with status ok") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsJson<Response<Status>>().shouldBe(Response(data = Status(status = "ok")))
        }
      }
    },
  )
