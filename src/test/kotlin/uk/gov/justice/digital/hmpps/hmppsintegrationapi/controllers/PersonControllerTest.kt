package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alias
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAddressesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageMetadataForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonsService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

@WebMvcTest(controllers = [PersonController::class])
internal class PersonControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getPersonService: GetPersonService,
  @MockBean val getPersonsService: GetPersonsService,
  @MockBean val getImageMetadataForPersonService: GetImageMetadataForPersonService,
  @MockBean val getAddressesForPersonService: GetAddressesForPersonService,
) : DescribeSpec({
  val pncId = "2003/13116M"
  val encodedPncId = URLEncoder.encode(pncId, StandardCharsets.UTF_8)

  describe("GET /persons") {
    val firstName = "Barry"
    val lastName = "Allen"

    beforeTest {
      Mockito.reset(getPersonsService)

      whenever(getPersonsService.execute(firstName, lastName)).thenReturn(
        listOf(
          Person(
            firstName = "Barry",
            lastName = "Allen",
            middleName = "Jonas",
            dateOfBirth = LocalDate.parse("2023-03-01"),
          ),
          Person(
            firstName = "Barry",
            lastName = "Allen",
            middleName = "Rock",
            dateOfBirth = LocalDate.parse("2022-07-22"),
          ),
        ),
      )
    }

    it("responds with a 200 OK status") {
      val result = mockMvc.perform(get("/persons?first_name=$firstName&last_name=$lastName")).andReturn()

      result.response.status.shouldBe(HttpStatus.OK.value())
    }

    it("returns an empty list embedded in a JSON object when no matching people") {
      val firstNameThatDoesNotExist = "Bob21345"
      val lastNameThatDoesNotExist = "Gun36773"

      whenever(getPersonsService.execute(firstNameThatDoesNotExist, lastNameThatDoesNotExist)).thenReturn(
        listOf(),
      )

      val result =
        mockMvc.perform(get("/persons?first_name=$firstNameThatDoesNotExist&last_name=$lastNameThatDoesNotExist"))
          .andReturn()

      result.response.contentAsString.shouldBe(
        """
          {
            "persons":[]
          }
          """.removeWhitespaceAndNewlines(),
      )
    }

    it("retrieves a person with matching search criteria") {
      mockMvc.perform(get("/persons?first_name=$firstName&last_name=$lastName")).andReturn()

      verify(getPersonsService, times(1)).execute(firstName, lastName)
    }

    it("returns a person with matching first and last name") {
      val result = mockMvc.perform(get("/persons?first_name=$firstName&last_name=$lastName")).andReturn()

      result.response.contentAsString.shouldBe(
        """
          {
            "persons":
            [
              {
                "firstName":"Barry",
                "lastName":"Allen",
                "middleName":"Jonas",
                "dateOfBirth":"2023-03-01",
                "aliases":[],
                "prisonerId": null
               },
               {
                 "firstName":"Barry",
                 "lastName":"Allen",
                 "middleName":"Rock",
                 "dateOfBirth":"2022-07-22",
                 "aliases":[],
                "prisonerId": null
               }
             ]
           }
        """.removeWhitespaceAndNewlines(),
      )
    }

    it("retrieves a person with matching first name") {
      mockMvc.perform(get("/persons?first_name=$firstName")).andReturn()

      verify(getPersonsService, times(1)).execute(firstName, null)
    }

    it("retrieves a person with matching last name") {
      mockMvc.perform(get("/persons?last_name=$lastName")).andReturn()

      verify(getPersonsService, times(1)).execute(null, lastName)
    }

    it("responds with a 400 BAD REQUEST status when no search criteria provided") {
      val result = mockMvc.perform(get("/persons")).andReturn()

      result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      result.response.contentAsString.shouldContain("No query parameters specified.")
    }
  }

  describe("GET /persons/{id}") {

    val person = mapOf(
      "nomis" to null,
      "prisonerOffenderSearch" to Person("Sally", "Sob"),
      "probationOffenderSearch" to Person("Silly", "Sobbers"),
    )

    beforeTest {
      Mockito.reset(getPersonService)
      whenever(getPersonService.execute(pncId)).thenReturn(person)
    }

    it("responds with a 200 OK status") {
      val result = mockMvc.perform(get("/persons/$encodedPncId")).andReturn()

      result.response.status.shouldBe(HttpStatus.OK.value())
    }

    it("responds with a 404 NOT FOUND status") {
      val idThatDoesNotExist = "9999/11111Z"
      whenever(getPersonService.execute(idThatDoesNotExist)).thenReturn(null)

      val encodedIdThatDoesNotExist = URLEncoder.encode(idThatDoesNotExist, StandardCharsets.UTF_8)
      val result = mockMvc.perform(get("/persons/$encodedIdThatDoesNotExist")).andReturn()

      result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
    }

    it("retrieves a person with the matching ID") {
      mockMvc.perform(get("/persons/$encodedPncId")).andReturn()

      verify(getPersonService, times(1)).execute(pncId)
    }

    it("returns a person with the matching ID") {
      val result = mockMvc.perform(get("/persons/$encodedPncId")).andReturn()

      result.response.contentAsString.shouldBe(
        """
        {
          "nomis": null,
          "prisonerOffenderSearch": {
            "firstName": "Sally",
            "lastName": "Sob",
            "middleName": null,
            "dateOfBirth": null,
            "aliases": [],
            "prisonerId":null
          },
          "probationOffenderSearch": {
            "firstName": "Silly",
            "lastName": "Sobbers",
            "middleName": null,
            "dateOfBirth": null,
            "aliases": [],
            "prisonerId":null
          }
        }
        """.removeWhitespaceAndNewlines(),
      )
    }
  }

  describe("GET /persons/$encodedPncId/images") {
    beforeTest {
      Mockito.reset(getImageMetadataForPersonService)
      whenever(getImageMetadataForPersonService.execute(pncId)).thenReturn(
        listOf(
          ImageMetadata(
            id = 2461788,
            captureDate = LocalDate.parse("2023-03-01"),
            view = "FACE",
            orientation = "FRONT",
            type = "OFF_BKG",
          ),
        ),
      )
    }

    it("responds with a 200 OK status") {
      val result = mockMvc.perform(get("/persons/$encodedPncId/images")).andReturn()

      result.response.status.shouldBe(HttpStatus.OK.value())
    }

    it("retrieves the metadata of images for a person with the matching ID") {
      mockMvc.perform(get("/persons/$encodedPncId/images")).andReturn()

      verify(getImageMetadataForPersonService, times(1)).execute(pncId)
    }

    it("returns the metadata of images for a person with the matching ID") {
      val result = mockMvc.perform(get("/persons/$encodedPncId/images")).andReturn()

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
        """.removeWhitespaceAndNewlines(),
      )
    }
  }

  describe("GET /persons/{encodedPncId}/addresses") {
    beforeTest {
      Mockito.reset(getAddressesForPersonService)
      whenever(getAddressesForPersonService.execute(pncId)).thenReturn(
        listOf(
          Address(postcode = "SE1 1TE"),
        ),
      )
    }

    it("responds with a 200 OK status") {
      val result = mockMvc.perform(get("/persons/$encodedPncId/addresses")).andReturn()

      result.response.status.shouldBe(HttpStatus.OK.value())
    }

    it("retrieves the addresses for a person with the matching ID") {
      mockMvc.perform(get("/persons/$encodedPncId/addresses")).andReturn()

      verify(getAddressesForPersonService, times(1)).execute(pncId)
    }

    it("returns the addresses for a person with the matching ID") {
      val result = mockMvc.perform(get("/persons/$encodedPncId/addresses")).andReturn()

      result.response.contentAsString.shouldBe(
        """
        {
          "addresses": [
            {
              "postcode": "SE1 1TE"
            }
          ]
        }
        """.removeWhitespaceAndNewlines(),
      )
    }

    it("responds with a 404 NOT FOUND status when person isn't found") {
      whenever(getAddressesForPersonService.execute(pncId)).thenReturn(null)

      val result = mockMvc.perform(get("/persons/$encodedPncId/addresses")).andReturn()

      result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
    }
  }
})
