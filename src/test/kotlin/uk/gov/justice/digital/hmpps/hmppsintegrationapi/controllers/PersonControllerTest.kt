package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory.times
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

@WebMvcTest(controllers = [PersonController::class])
internal class PersonControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getPersonService: GetPersonService
) : DescribeSpec({
  describe("GET /persons/{id}") {
    val id = 1

    beforeTest {
      Mockito.reset(getPersonService);
    }

    it("responds with a 200 OK status") {
      val result = mockMvc.perform(get("/persons/$id")).andReturn();

      result.response.status.shouldBe(200);
    }

    it("retrieves a person with the matching ID") {
      mockMvc.perform(get("/persons/$id")).andReturn();

      verify(getPersonService, times(1)).execute(id);
    }
  }
})
