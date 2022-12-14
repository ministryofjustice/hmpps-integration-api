package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest
internal class PersonControllerTest(@Autowired val mockMvc: MockMvc) : DescribeSpec({
  describe("GET /persons/{id}") {
    it("responds with a 200 OK status") {
      val id=1
      val result = mockMvc.perform(get("/persons/$id")).andReturn();

      result.response.status.shouldBe(200);
    }
  }
})
