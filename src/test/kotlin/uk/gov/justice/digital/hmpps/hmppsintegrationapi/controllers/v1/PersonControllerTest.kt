package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
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
  val basePath = "/v1/persons"
  val firstName = "Barry"
  val lastName = "Allen"

  describe("GET $basePath") {
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
      val result = mockMvc.perform(get("$basePath?first_name=$firstName&last_name=$lastName")).andReturn()

      result.response.status.shouldBe(HttpStatus.OK.value())
    }

    it("returns an empty list embedded in a JSON object when no matching people") {
      val firstNameThatDoesNotExist = "Bob21345"
      val lastNameThatDoesNotExist = "Gun36773"

      whenever(getPersonsService.execute(firstNameThatDoesNotExist, lastNameThatDoesNotExist)).thenReturn(
        listOf(),
      )

      val result =
        mockMvc.perform(get("$basePath?first_name=$firstNameThatDoesNotExist&last_name=$lastNameThatDoesNotExist"))
          .andReturn()

      result.response.contentAsString.shouldContain(
        """
          "content":[]
        """.removeWhitespaceAndNewlines(),
      )
    }

    it("retrieves a person with matching search criteria") {
      mockMvc.perform(get("$basePath?first_name=$firstName&last_name=$lastName")).andReturn()

      verify(getPersonsService, times(1)).execute(firstName, lastName)
    }

    it("returns a person with matching first and last name") {
      val result = mockMvc.perform(get("$basePath?first_name=$firstName&last_name=$lastName")).andReturn()

      result.response.contentAsString.shouldContain(
        """
          {
            "content":
            [
              {
                "firstName":"Barry",
                "lastName":"Allen",
                "middleName":"Jonas",
                "dateOfBirth":"2023-03-01",
                "aliases":[],
                "prisonerId": null,
                "pncId": null
               },
               {
                 "firstName":"Barry",
                 "lastName":"Allen",
                 "middleName":"Rock",
                 "dateOfBirth":"2022-07-22",
                 "aliases":[],
                 "prisonerId": null,
                 "pncId": null
               }
             ]
        """.removeWhitespaceAndNewlines(),
      )
    }

    it("retrieves a person with matching first name") {
      mockMvc.perform(get("$basePath?first_name=$firstName")).andReturn()

      verify(getPersonsService, times(1)).execute(firstName, null)
    }

    it("retrieves a person with matching last name") {
      mockMvc.perform(get("$basePath?last_name=$lastName")).andReturn()

      verify(getPersonsService, times(1)).execute(null, lastName)
    }

    it("responds with a 400 BAD REQUEST status when no search criteria provided") {
      val result = mockMvc.perform(get(basePath)).andReturn()

      result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      result.response.contentAsString.shouldContain("No query parameters specified.")
    }
  }

  describe("GET $basePath/{id}") {
    val person = mapOf(
      "prisonerOffenderSearch" to Person("Sally", "Sob"),
      "probationOffenderSearch" to Person("Silly", "Sobbers"),
    )

    beforeTest {
      Mockito.reset(getPersonService)
      whenever(getPersonService.execute(pncId)).thenReturn(person)
    }

    it("responds with a 200 OK status") {
      val result = mockMvc.perform(get("$basePath/$encodedPncId")).andReturn()

      result.response.status.shouldBe(HttpStatus.OK.value())
    }

    it("responds with a 404 NOT FOUND status") {
      val idThatDoesNotExist = "9999/11111Z"
      whenever(getPersonService.execute(idThatDoesNotExist)).thenReturn(null)

      val encodedIdThatDoesNotExist = URLEncoder.encode(idThatDoesNotExist, StandardCharsets.UTF_8)
      val result = mockMvc.perform(get("$basePath/$encodedIdThatDoesNotExist")).andReturn()

      result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
    }

    it("retrieves a person with the matching ID") {
      mockMvc.perform(get("$basePath/$encodedPncId")).andReturn()

      verify(getPersonService, times(1)).execute(pncId)
    }

    it("returns a person with the matching ID") {
      val result = mockMvc.perform(get("$basePath/$encodedPncId")).andReturn()

      result.response.contentAsString.shouldBe(
        """
        {
          "prisonerOffenderSearch": {
            "firstName": "Sally",
            "lastName": "Sob",
            "middleName": null,
            "dateOfBirth": null,
            "aliases": [],
            "prisonerId": null,
            "pncId": null
          },
          "probationOffenderSearch": {
            "firstName": "Silly",
            "lastName": "Sobbers",
            "middleName": null,
            "dateOfBirth": null,
            "aliases": [],
            "prisonerId": null,
            "pncId": null
          }
        }
        """.removeWhitespaceAndNewlines(),
      )
    }
  }

  describe("GET $basePath/$encodedPncId/images") {
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
      val result = mockMvc.perform(get("$basePath/$encodedPncId/images")).andReturn()

      result.response.status.shouldBe(HttpStatus.OK.value())
    }

    it("retrieves the metadata of images for a person with the matching ID") {
      mockMvc.perform(get("$basePath/$encodedPncId/images")).andReturn()

      verify(getImageMetadataForPersonService, times(1)).execute(pncId)
    }

    it("returns the metadata of images for a person with the matching ID") {
      val result = mockMvc.perform(get("$basePath/$encodedPncId/images")).andReturn()

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

  describe("GET $basePath/{encodedPncId}/addresses") {
    beforeTest {
      Mockito.reset(getAddressesForPersonService)
      whenever(getAddressesForPersonService.execute(pncId)).thenReturn(
        listOf(
          Address(postcode = "SE1 1TE"),
        ),
      )
    }

    it("responds with a 200 OK status") {
      val result = mockMvc.perform(get("$basePath/$encodedPncId/addresses")).andReturn()

      result.response.status.shouldBe(HttpStatus.OK.value())
    }

    it("retrieves the addresses for a person with the matching ID") {
      mockMvc.perform(get("$basePath/$encodedPncId/addresses")).andReturn()

      verify(getAddressesForPersonService, times(1)).execute(pncId)
    }

    it("returns the addresses for a person with the matching ID") {
      val result = mockMvc.perform(get("$basePath/$encodedPncId/addresses")).andReturn()

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

      val result = mockMvc.perform(get("$basePath/$encodedPncId/addresses")).andReturn()

      result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
    }
  }

  describe("Paginated results") {
    it("returns pagination information for empty results") {
      val firstNameThatDoesNotExist = "Bob21345"
      val lastNameThatDoesNotExist = "Gun36773"

      whenever(getPersonsService.execute(firstNameThatDoesNotExist, lastNameThatDoesNotExist)).thenReturn(
        listOf(),
      )

      val result =
        mockMvc.perform(get("$basePath?first_name=$firstNameThatDoesNotExist&last_name=$lastNameThatDoesNotExist"))
          .andReturn()

      result.response.contentAsString.shouldBe("""
        {
          "content": [],
          "pageable": {
          "sort": {
          "empty": true,
          "sorted": false,
          "unsorted": true
        },
          "offset": 0,
          "pageNumber": 0,
          "pageSize": 10,
          "paged": true,
          "unpaged": false
        },
          "totalPages": 0,
          "totalElements": 0,
          "last": true,
          "size": 10,
          "number": 0,
          "sort": {
          "empty": true,
          "sorted": false,
          "unsorted": true
        },
          "numberOfElements": 0,
          "first": true,
          "empty": true
        }
        """.removeWhitespaceAndNewlines())

//      result.response.contentAsString.shouldContain("\"offset\":0")
//      result.response.contentAsString.shouldContain("\"pageNumber\":0")
//      result.response.contentAsString.shouldContain("\"pageSize\":10")
//      result.response.contentAsString.shouldContain("\"paged\":true")
//      result.response.contentAsString.shouldContain("\"totalPages\":0")
//      result.response.contentAsString.shouldContain("\"totalElements\":0")
//      result.response.contentAsString.shouldContain("\"last\":true")
//      result.response.contentAsString.shouldContain("\"size\":10")
//      result.response.contentAsString.shouldContain("\"number\":0")
//      result.response.contentAsString.shouldContain("\"numberOfElements\":0")
//      result.response.contentAsString.shouldContain("\"first\":true")
//      result.response.contentAsString.shouldContain("\"empty\":true")
    }

    it("returns pagination information for one page of results") {
      val list = List(3) { i ->
        Person(
          firstName = "Barry $i",
          lastName = "Allen $i",
          middleName = "Jonas $i",
          dateOfBirth = LocalDate.parse("2023-03-01"),
        )
      }

      whenever(getPersonsService.execute(firstName, lastName)).thenReturn(list)

      val result =
        mockMvc.perform(get("$basePath?first_name=$firstName&last_name=$lastName"))
          .andReturn()

      result.response.contentAsString.shouldContain("\"totalPages\":1")
      result.response.contentAsString.shouldContain("\"totalElements\":3")
      result.response.contentAsString.shouldContain("\"last\":true")
    }

    it("returns pagination information for two pages of results") {
      val list = List(20) { i ->
        Person(
          firstName = "Barry $i",
          lastName = "Allen $i",
          dateOfBirth = LocalDate.parse("2023-03-01"),
        )
      }

      whenever(getPersonsService.execute(firstName, lastName)).thenReturn(list)

      val resultPage0 =
        mockMvc.perform(get("$basePath?first_name=$firstName&last_name=$lastName"))
          .andReturn()

      resultPage0.response.contentAsString.shouldContain("\"totalPages\":2")
      resultPage0.response.contentAsString.shouldContain("\"totalElements\":20")
      resultPage0.response.contentAsString.shouldContain("\"last\":false")
      resultPage0.response.contentAsString.shouldContain("\"first\":true")
      resultPage0.response.contentAsString.shouldContain("Barry 1")
      resultPage0.response.contentAsString.shouldNotContain("Barry 11")

      val resultPage1 =
        mockMvc.perform(get("$basePath?first_name=$firstName&last_name=$lastName&page=1"))
          .andReturn()

      resultPage1.response.contentAsString.shouldContain("\"last\":true")
      resultPage1.response.contentAsString.shouldContain("\"first\":false")
      resultPage1.response.contentAsString.shouldNotContain("Barry 1\"")
      resultPage1.response.contentAsString.shouldContain("Barry 19")
    }

    it("returns an empty list when a page requested is out of bounds") {
      val pageNumberThatDoesntExist = 99
      val list = listOf(
        Person(
          firstName = "Barry",
          lastName = "Allen",
          dateOfBirth = LocalDate.parse("2023-03-01"),
        ),
      )

      whenever(getPersonsService.execute(firstName, lastName)).thenReturn(list)

      val result =
        mockMvc.perform(get("$basePath?first_name=$firstName&last_name=$lastName&page=$pageNumberThatDoesntExist"))
          .andReturn()

      result.response.contentAsString.shouldContain("\"content\":[]")
    }
  }
},)
