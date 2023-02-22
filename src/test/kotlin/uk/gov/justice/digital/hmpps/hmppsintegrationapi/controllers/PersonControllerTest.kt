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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageMetadataForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import java.time.LocalDate

@WebMvcTest(controllers = [PersonController::class])
internal class PersonControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getPersonService: GetPersonService,
  @MockBean val getImageMetadataForPersonService: GetImageMetadataForPersonService
) : DescribeSpec({

  describe("GET /person") {
    val firstName = "Barry"
    val lastName = "Allen"

    beforeTest {
      Mockito.reset(getPersonService)
      whenever(getPersonService.execute(firstName, lastName)).thenReturn(
        listOf(
          Person(
            firstName = "Oliver",
            lastName = "Queen",
            middleName = "Jonas",
            dateOfBirth = LocalDate.parse("2023-03-01")
          ),
          Person(
            firstName = "Fred",
            lastName = "Flinstone",
            middleName = "Rock",
            dateOfBirth = LocalDate.parse("2022-07-22")
          )
        )
      )
    }

    it("responds with a 200 OK status") {
      val result = mockMvc.perform(get("/persons?firstName=$firstName,lastName=$lastName")).andReturn()

      result.response.status.shouldBe(200)
    }
  }

  val id = "abc123"
  describe("GET /persons/{id}") {
    val person = mapOf(
      "nomis" to Person(
        "Billy",
        "Bob",
        dateOfBirth = LocalDate.parse("1970-10-10"),
        aliases = listOf(Alias("Bill", "Bobbers", dateOfBirth = LocalDate.parse("1970-03-01")))
      ),
      "prisonerOffenderSearch" to Person("Sally", "Sob"),
      "probationOffenderSearch" to Person("Silly", "Sobbers")
    )

    beforeTest {
      Mockito.reset(getPersonService)
      whenever(getPersonService.execute(id)).thenReturn(person)
    }

    it("responds with a 200 OK status") {
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
      val result = mockMvc.perform(get("/persons/$id")).andReturn()

      result.response.contentAsString.shouldBe(
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
          },
          "probationOffenderSearch": {
            "firstName": "Silly",
            "lastName": "Sobbers",
            "middleName": null,
            "dateOfBirth": null,
            "aliases": []
          }
        }
        """.removeWhitespaceAndNewlines()
      )
    }
  }

  describe("GET /persons/{id}/images") {
    beforeTest {
      Mockito.reset(getImageMetadataForPersonService)
      whenever(getImageMetadataForPersonService.execute(id)).thenReturn(
        listOf(
          ImageMetadata(
            id = 2461788,
            captureDate = LocalDate.parse("2023-03-01"),
            view = "FACE",
            orientation = "FRONT",
            type = "OFF_BKG"
          )
        )
      )
    }

    it("responds with a 200 OK status") {
      val result = mockMvc.perform(get("/persons/$id/images")).andReturn()

      result.response.status.shouldBe(200)
    }

    it("retrieves the metadata of images for a person with the matching ID") {
      mockMvc.perform(get("/persons/$id/images")).andReturn()

      verify(getImageMetadataForPersonService, times(1)).execute(id)
    }

    it("returns the metadata of images for a person with the matching ID") {
      val result = mockMvc.perform(get("/persons/$id/images")).andReturn()

      result.response.contentAsString.shouldBe(
        """
        {
          "images": [
            {
              "id" : 2461788,
              "captureDate": "2023-03-01",
              "view": "FACE",
              "orientation": "FRONT",
              "type": "OFF_BKG"
            }
          ]
        }
        """.removeWhitespaceAndNewlines()
      )
    }
  }
})
