package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAddressesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageMetadataForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonsService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

@WebMvcTest(controllers = [PersonController::class])
internal class PersonControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getPersonService: GetPersonService,
  @MockBean val getPersonsService: GetPersonsService,
  @MockBean val getImageMetadataForPersonService: GetImageMetadataForPersonService,
  @MockBean val getAddressesForPersonService: GetAddressesForPersonService,
) : DescribeSpec(
  {
    val pncId = "2003/13116M"
    val encodedPncId = URLEncoder.encode(pncId, StandardCharsets.UTF_8)
    val basePath = "/v1/persons"
    val firstName = "Barry"
    val lastName = "Allen"

    describe("GET $basePath") {
      beforeTest {
        Mockito.reset(getPersonsService)

        whenever(getPersonsService.execute(firstName, lastName)).thenReturn(
          Response(
            data =
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
          ),
        )
      }

      it("retrieves a person with matching search criteria") {
        mockMvc.perform(get("$basePath?first_name=$firstName&last_name=$lastName")).andReturn()

        verify(getPersonsService, times(1)).execute(firstName, lastName)
      }

      it("retrieves a person with matching first name") {
        mockMvc.perform(get("$basePath?first_name=$firstName")).andReturn()

        verify(getPersonsService, times(1)).execute(firstName, null)
      }

      it("retrieves a person with matching last name") {
        mockMvc.perform(get("$basePath?last_name=$lastName")).andReturn()

        verify(getPersonsService, times(1)).execute(null, lastName)
      }

      it("retrieves a person with matching alias") {
        mockMvc.perform(get("$basePath?first_name=$firstName&search_within_aliases=true")).andReturn()

        verify(getPersonsService, times(1)).execute(firstName, null, searchWithinAliases = true)
      }

      it("defaults to not searching within aliases") {
        mockMvc.perform(get("$basePath?first_name=$firstName")).andReturn()

        verify(getPersonsService, times(1)).execute(firstName, null, searchWithinAliases = false)
      }

      it("returns a person with matching first and last name") {
        val result = mockMvc.perform(get("$basePath?first_name=$firstName&last_name=$lastName")).andReturn()

        result.response.contentAsString.shouldContain(
          """
          "data": [
            {
              "firstName":"Barry",
              "lastName":"Allen",
              "middleName":"Jonas",
              "dateOfBirth":"2023-03-01",
              "gender": null,
              "ethnicity": null,
              "aliases":[],
              "identifiers": {
                  "nomisNumber": null,
                  "croNumber": null,
                  "deliusCrn": null
              },
              "pncId": null
            },
            {
              "firstName":"Barry",
              "lastName":"Allen",
              "middleName":"Rock",
              "dateOfBirth":"2022-07-22",
              "gender": null,
              "ethnicity": null,
              "aliases":[],
              "identifiers": {
                  "nomisNumber": null,
                  "croNumber": null,
                  "deliusCrn": null
              },
              "pncId": null
            }
          ]
          """.removeWhitespaceAndNewlines(),
        )
      }

      it("returns paginated results") {
        whenever(getPersonsService.execute(firstName, lastName)).thenReturn(
          Response(
            data =
            List(20) { i ->
              Person(
                firstName = "Barry $i",
                lastName = "Allen $i",
                dateOfBirth = LocalDate.parse("2023-03-01"),
              )
            },
          ),
        )

        val result =
          mockMvc.perform(get("$basePath?first_name=$firstName&last_name=$lastName&page=3&perPage=5"))
            .andReturn()

        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 3)
        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 4)
      }

      it("returns an empty list embedded in a JSON object when no matching people") {
        val firstNameThatDoesNotExist = "Bob21345"
        val lastNameThatDoesNotExist = "Gun36773"

        whenever(getPersonsService.execute(firstNameThatDoesNotExist, lastNameThatDoesNotExist)).thenReturn(
          Response(
            data = emptyList(),
          ),
        )

        val result =
          mockMvc.perform(get("$basePath?first_name=$firstNameThatDoesNotExist&last_name=$lastNameThatDoesNotExist"))
            .andReturn()

        result.response.contentAsString.shouldContain("\"data\":[]".removeWhitespaceAndNewlines())
      }

      it("responds with a 200 OK status") {
        val result = mockMvc.perform(get("$basePath?first_name=$firstName&last_name=$lastName")).andReturn()

        result.response.status.shouldBe(HttpStatus.OK.value())
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
        whenever(getPersonService.execute(pncId)).thenReturn(Response(data = person))
      }

      it("responds with a 200 OK status") {
        val result = mockMvc.perform(get("$basePath/$encodedPncId")).andReturn()

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      describe("404 Not found") {
        val idThatDoesNotExist = "9999/11111Z"

        it("responds with a 404 when a person cannot be found in both upstream APIs") {
          whenever(getPersonService.execute(idThatDoesNotExist)).thenReturn(
            Response(
              mapOf(
                "prisonerOffenderSearch" to null,
                "probationOffenderSearch" to null,
              ),
              errors = listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
                UpstreamApiError(
                  causedBy = UpstreamApi.NOMIS,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
            ),
          )

          val encodedIdThatDoesNotExist = URLEncoder.encode(idThatDoesNotExist, StandardCharsets.UTF_8)
          val result = mockMvc.perform(get("$basePath/$encodedIdThatDoesNotExist")).andReturn()

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("does not respond with a 404 when a person was found in one upstream API") {
          whenever(getPersonService.execute(idThatDoesNotExist)).thenReturn(
            Response(
              mapOf(
                "probationOffenderSearch" to Person("someFirstName", "someLastName"),
                "prisonerOffenderSearch" to null,
              ),
              errors = listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.NOMIS,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
            ),
          )

          val encodedIdThatDoesNotExist = URLEncoder.encode(idThatDoesNotExist, StandardCharsets.UTF_8)
          val result = mockMvc.perform(get("$basePath/$encodedIdThatDoesNotExist")).andReturn()

          result.response.status.shouldNotBe(HttpStatus.NOT_FOUND.value())
        }
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
            "gender": null,
            "ethnicity": null,
            "aliases": [],
            "identifiers": {
                  "nomisNumber": null,
                  "croNumber": null,
                  "deliusCrn": null
            },              
            "pncId": null
          },
          "probationOffenderSearch": {
            "firstName": "Silly",
            "lastName": "Sobbers",
            "middleName": null,
            "dateOfBirth": null,
            "gender": null,
            "ethnicity": null,
            "aliases": [],
            "identifiers": {
                "nomisNumber": null,
                "croNumber": null,
                "deliusCrn": null
            },
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
          Response(
            data = listOf(
              ImageMetadata(
                id = 2461788,
                active = true,
                captureDateTime = LocalDateTime.parse("2023-03-01T13:20:00"),
                view = "FACE",
                orientation = "FRONT",
                type = "OFF_BKG",
              ),
            ),
          ),
        )
      }

      it("returns paginated results") {
        whenever(getImageMetadataForPersonService.execute(pncId)).thenReturn(
          Response(
            data = List(20) {
              ImageMetadata(
                id = Random.nextLong(),
                active = Random.nextBoolean(),
                captureDateTime = LocalDateTime.now(),
                view = "OIC",
                orientation = "NECK",
                type = "OFF_IDM",
              )
            },
          ),
        )

        val result =
          mockMvc.perform(get("$basePath/$encodedPncId/images?page=3&perPage=5"))
            .andReturn()

        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 3)
        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 4)
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
        result.response.contentAsString.shouldContain("\"data\":[")
        result.response.contentAsString.shouldContain(
          """
            "id" : 2461788,
              "active" : true,
              "captureDateTime": "2023-03-01T13:20:00",
              "view": "FACE",
              "orientation": "FRONT",
              "type": "OFF_BKG"
          """.removeWhitespaceAndNewlines(),
        )
      }

      it("responds with a 404 NOT FOUND status") {
        whenever(getImageMetadataForPersonService.execute(pncId)).thenReturn(
          Response(
            data = emptyList(),
            errors = listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.NOMIS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
          ),
        )

        val result = mockMvc.perform(get("$basePath/$encodedPncId/images")).andReturn()

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }
    }

    describe("GET $basePath/{encodedPncId}/addresses") {
      beforeTest {
        Mockito.reset(getAddressesForPersonService)
        whenever(getAddressesForPersonService.execute(pncId)).thenReturn(Response(data = listOf(generateTestAddress())))
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
          "data": [
            {
              "country": "England",
              "county": "Greater London",
              "endDate": "2023-05-20",
              "locality": "London Bridge",
              "name": "The chocolate factory",
              "noFixedAddress": false,
              "number": "89",
              "postcode": "SE1 1TZ",
              "startDate": "2021-05-10",
              "street": "Omeara",
              "town": "London Town",
              "types": [
                {
                  "code": "A99",
                  "description": "Chocolate Factory"
                },
                {
                  "code": "B99",
                  "description": "Glass Elevator"
                }
              ],
              "notes": "some interesting note"
            }
          ]
        }
        """.removeWhitespaceAndNewlines(),
        )
      }

      it("responds with a 404 NOT FOUND status when person isn't found in all upstream APIs") {
        whenever(getAddressesForPersonService.execute(pncId)).thenReturn(
          Response(
            data = emptyList(),
            errors = listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.NOMIS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
              UpstreamApiError(
                causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
          ),
        )

        val result = mockMvc.perform(get("$basePath/$encodedPncId/addresses")).andReturn()

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("responds with a 200 OK status when person is found in one upstream API but not another") {
        whenever(getAddressesForPersonService.execute(pncId)).thenReturn(
          Response(
            data = emptyList(),
            errors = listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.NOMIS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
          ),
        )

        val result = mockMvc.perform(get("$basePath/$encodedPncId/addresses")).andReturn()

        result.response.status.shouldBe(HttpStatus.OK.value())
      }
    }
  },
)
