package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import org.json.JSONObject
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

@WebMvcTest(controllers = [PersonController::class])
internal class PersonControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getPersonService: GetPersonService
) : DescribeSpec({
  describe("GET /persons/{id}") {
    val id = 1

    beforeTest {
      Mockito.reset(getPersonService)
    }

    it("responds with a 200 OK status") {
      val person = Person(id, "Billy", "Bob")
      Mockito.`when`(getPersonService.execute(id)).thenReturn(person)
      val result = mockMvc.perform(get("/persons/$id")).andReturn()

      result.response.status.shouldBe(200)
    }

    @Throws
    it("responds with a 404 NOT FOUND status") {
      val id_that_does_not_exist = 777
      val result = mockMvc.perform(get("/persons/$id_that_does_not_exist")).andReturn()

      result.response.status.shouldBe(404)
    }

    it("retrieves a person with the matching ID") {
      mockMvc.perform(get("/persons/$id")).andReturn()

      verify(getPersonService, times(1)).execute(id)
    }

    it("returns a person with the matching ID") {
      val person = Person(id, "Billy", "Bob")
      Mockito.`when`(getPersonService.execute(id)).thenReturn(person)

      val result = mockMvc.perform(get("/persons/$id")).andReturn()

      result.response.contentAsString.shouldNotBeEmpty()
      JSONObject(result.response.contentAsString)["id"].shouldBe(person.id)
      JSONObject(result.response.contentAsString)["firstName"].shouldBe(person.firstName)
      JSONObject(result.response.contentAsString)["lastName"].shouldBe(person.lastName)
    }
  }
})
