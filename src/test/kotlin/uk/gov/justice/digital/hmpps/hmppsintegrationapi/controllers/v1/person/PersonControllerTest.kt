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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

@WebMvcTest(controllers = [PersonController::class])
@ActiveProfiles("test")
internal class PersonControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockBean val getPersonService: GetPersonService,
  @MockBean val getPersonsService: GetPersonsService,
  @MockBean val getImageMetadataForPersonService: GetImageMetadataForPersonService,
  @MockBean val getAddressesForPersonService: GetAddressesForPersonService,
  @MockBean val auditService: AuditService,
) : DescribeSpec(
  {
    val hmppsId = "2003/13116M"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val basePath = "/v1/persons"
    val firstName = "Barry"
    val lastName = "Allen"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    describe("GET $basePath") {
      beforeTest {
        Mockito.reset(getPersonsService)
        Mockito.reset(auditService)
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

      it("gets a person with matching search criteria") {
        mockMvc.performAuthorised("$basePath?first_name=$firstName&last_name=$lastName")

        verify(getPersonsService, times(1)).execute(firstName, lastName)
      }

      it("gets a person with matching first name") {
        mockMvc.performAuthorised("$basePath?first_name=$firstName")

        verify(getPersonsService, times(1)).execute(firstName, null)
      }

      it("gets a person with matching last name") {
        mockMvc.performAuthorised("$basePath?last_name=$lastName")

        verify(getPersonsService, times(1)).execute(null, lastName)
      }

      it("gets a person with matching alias") {
        mockMvc.performAuthorised("$basePath?first_name=$firstName&search_within_aliases=true")

        verify(getPersonsService, times(1)).execute(firstName, null, searchWithinAliases = true)
      }

      it("defaults to not searching within aliases") {
        mockMvc.performAuthorised("$basePath?first_name=$firstName")

        verify(getPersonsService, times(1)).execute(firstName, null, searchWithinAliases = false)
      }

      it("returns a person with matching first and last name") {
        val result = mockMvc.performAuthorised("$basePath?first_name=$firstName&last_name=$lastName")
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
              "pncId": null,
              "hmppsId": null
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
              "pncId": null,
              "hmppsId": null
            }
          ]
          """.removeWhitespaceAndNewlines(),
        )
      }

      it("logs audit") {
        mockMvc.performAuthorised("$basePath?first_name=$firstName&last_name=$lastName")
        verify(auditService, times(1)).createEvent("SEARCH_PERSON", "Person searched with first name: $firstName, last name: $lastName and search within aliases: false")
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

        val result = mockMvc.performAuthorised("$basePath?first_name=$firstName&last_name=$lastName&page=3&perPage=5")

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

        val result = mockMvc.performAuthorised("$basePath?first_name=$firstNameThatDoesNotExist&last_name=$lastNameThatDoesNotExist")

        result.response.contentAsString.shouldContain("\"data\":[]".removeWhitespaceAndNewlines())
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised("$basePath?first_name=$firstName&last_name=$lastName")

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("returns a 400 BAD REQUEST status code when no search criteria provided") {
        val result = mockMvc.performAuthorised(basePath)

        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        result.response.contentAsString.shouldContain("No query parameters specified.")
      }
    }

    describe("GET $basePath/{id}") {
      val person = Person("Silly", "Sobbers")

      beforeTest {
        Mockito.reset(getPersonService)
        Mockito.reset(auditService)
        whenever(getPersonService.execute(hmppsId)).thenReturn(Response(data = person))
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId")

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("logs audit") {
        mockMvc.performAuthorised("$basePath/$encodedHmppsId")
        verify(auditService, times(1)).createEvent("GET_PERSON_DETAILS", "Person details with hmpps id: $hmppsId has been retrieved")
      }

      describe("404 Not found") {
        beforeTest {
          Mockito.reset(auditService)
        }
        val idThatDoesNotExist = "9999/11111Z"

        it("returns a 404 status code when a person cannot be found in both upstream APIs") {
          whenever(getPersonService.execute(idThatDoesNotExist)).thenReturn(
            Response(
              data = null,
              errors = listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
            ),
          )

          val encodedIdThatDoesNotExist = URLEncoder.encode(idThatDoesNotExist, StandardCharsets.UTF_8)
          val result = mockMvc.performAuthorised("$basePath/$encodedIdThatDoesNotExist")

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("does not return a 404 status code when a person was found in one upstream API") {
          whenever(getPersonService.execute(idThatDoesNotExist)).thenReturn(
            Response(
              data = Person("someFirstName", "someLastName"),
              errors = listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.NOMIS,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
            ),
          )

          val encodedIdThatDoesNotExist = URLEncoder.encode(idThatDoesNotExist, StandardCharsets.UTF_8)
          val result = mockMvc.performAuthorised("$basePath/$encodedIdThatDoesNotExist")

          result.response.status.shouldNotBe(HttpStatus.NOT_FOUND.value())
        }
      }

      it("gets a person with the matching ID") {
        mockMvc.performAuthorised("$basePath/$encodedHmppsId")

        verify(getPersonService, times(1)).execute(hmppsId)
      }

      it("returns a person with the matching ID") {
        val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId")

        result.response.contentAsString.shouldBe(
          """
        {
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
          "pncId": null,
          "hmppsId": null
        }
        """.removeWhitespaceAndNewlines(),
        )
      }
    }

    describe("GET $basePath/$encodedHmppsId/images") {
      beforeTest {
        Mockito.reset(auditService)
        Mockito.reset(getImageMetadataForPersonService)
        whenever(getImageMetadataForPersonService.execute(hmppsId)).thenReturn(
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
        whenever(getImageMetadataForPersonService.execute(hmppsId)).thenReturn(
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

        val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId/images?page=3&perPage=5")
        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 3)
        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 4)
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId/images")
        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("logs audit") {
        mockMvc.performAuthorised("$basePath/$encodedHmppsId/images")
        verify(auditService, times(1)).createEvent("GET_PERSON_IMAGE", "Image with id: $hmppsId has been retrieved")
      }

      it("gets the metadata of images for a person with the matching ID") {
        mockMvc.performAuthorised("$basePath/$encodedHmppsId/images")

        verify(getImageMetadataForPersonService, times(1)).execute(hmppsId)
      }

      it("returns the metadata of images for a person with the matching ID") {
        val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId/images")
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

      it("returns a 404 NOT FOUND status code") {
        whenever(getImageMetadataForPersonService.execute(hmppsId)).thenReturn(
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

        val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId/images")

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }
    }

    describe("GET $basePath/{encodedHmppsId}/addresses") {
      beforeTest {
        Mockito.reset(getAddressesForPersonService)
        Mockito.reset(auditService)
        whenever(getAddressesForPersonService.execute(hmppsId)).thenReturn(Response(data = listOf(generateTestAddress())))
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId/addresses")

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("gets the addresses for a person with the matching ID") {
        mockMvc.performAuthorised("$basePath/$encodedHmppsId/addresses")

        verify(getAddressesForPersonService, times(1)).execute(hmppsId)
      }

      it("returns the addresses for a person with the matching ID") {
        val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId/addresses")
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

      it("logs audit") {
        mockMvc.performAuthorised("$basePath/$encodedHmppsId/addresses")
        verify(auditService, times(1)).createEvent("GET_PERSON_ADDRESS", "Person address details with hmpps id: $hmppsId has been retrieved")
      }

      it("returns a 404 NOT FOUND status code when person isn't found in all upstream APIs") {
        whenever(getAddressesForPersonService.execute(hmppsId)).thenReturn(
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

        val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId/addresses")

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("returns a 200 OK status code when person is found in one upstream API but not another") {
        whenever(getAddressesForPersonService.execute(hmppsId)).thenReturn(
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

        val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId/addresses")

        result.response.status.shouldBe(HttpStatus.OK.value())
      }
    }
  },
)
