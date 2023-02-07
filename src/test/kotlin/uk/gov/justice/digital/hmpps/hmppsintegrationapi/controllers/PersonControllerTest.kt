package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alias
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import java.time.LocalDate

@WebMvcTest(controllers = [PersonController::class])
internal class PersonControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getPersonService: GetPersonService
) : DescribeSpec({
  describe("GET /persons/{id}") {
    val id = "abc123"

    beforeTest {
      Mockito.reset(getPersonService)
    }

    it("responds with a 200 OK status") {
      val person = mapOf(
        "nomis" to Person("Billy", "Bob"),
        "prisonerOffenderSearch" to Person("Sally", "Sob")
      )
      whenever(getPersonService.execute(id)).thenReturn(person)

      val result = mockMvc.perform(get("/persons/$id")).andReturn()

      result.response.status.shouldBe(200)
    }

    it("responds with a 404 NOT FOUND status") {
      val idThatDoesNotExist = "zyx987"
      whenever(getPersonService.execute(id)).thenReturn(null)

      val result = mockMvc.perform(get("/persons/$idThatDoesNotExist")).andReturn()

      result.response.status.shouldBe(404)
    }

    it("retrieves a person with the matching ID") {
      mockMvc.perform(get("/persons/$id")).andReturn()

      verify(getPersonService, times(1)).execute(id)
    }

    it("returns a person with the matching ID") {
      val stubbedResponse = mapOf<String, Person?>(
        "nomis" to Person(
          "Billy",
          "Bob",
          dateOfBirth = LocalDate.parse("1970-10-10"),
          aliases = listOf(Alias("Bill", "Bobbers", dateOfBirth = LocalDate.parse("1970-03-01")))
        ),
        "prisonerOffenderSearch" to Person("Sally", "Sob")
      )

      whenever(getPersonService.execute(id)).thenReturn(stubbedResponse)

      val expectedResult = mockMvc.perform(get("/persons/$id")).andReturn()
      println(expectedResult.response.contentAsString)
      expectedResult.response.contentAsString.shouldBe(
        """
         {
          "nomis": {
            "firstName": "Billy",
            "lastName": "Bob",
            "middleName": null,
            "dateOfBirth": "1970-10-10",
            "aliases": [
              {
                "firstName": "Bill",
                "lastName": "Bobbers",
                "middleName": null,
                "dateOfBirth": "1970-03-01"
              }
            ]
          },
          "prisonerOffenderSearch": {
            "firstName": "Sally",
            "lastName": "Sob",
            "middleName": null,
            "dateOfBirth": null,
            "aliases": []
          }
        }
        """.removeWhitespaceAndNewlines()
      )
    }
  }
})
