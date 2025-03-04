package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory.times
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Contact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactDetailsWithEmailAndPhone
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedPrisonerContacts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonOnProbation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PhoneNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContactRelationship
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitOrders
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageMetadataForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetNameForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonerContactsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitOrdersForPersonService
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
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val getPersonsService: GetPersonsService,
  @MockitoBean val getNameForPersonService: GetNameForPersonService,
  @MockitoBean val getImageMetadataForPersonService: GetImageMetadataForPersonService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getPrisonerContactsService: GetPrisonerContactsService,
  @MockitoBean val getVisitOrdersForPersonService: GetVisitOrdersForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "2003/13116M"
      val sanitisedHmppsId = "200313116M"
      val pncNumber = "2003/13116M"
      val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
      val basePath = "/v1/persons"
      val firstName = "Barry"
      val lastName = "Allen"
      val dateOfBirth = "2023-03-01"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val phoneNumbers: List<PhoneNumber> =
        listOf(
          PhoneNumber("07123456789", "Mobile"),
          PhoneNumber("01611234567", "Landline"),
        )
      val emails: List<String> = listOf("barry.allen@starlabs.gov")

      describe("GET $basePath") {
        beforeTest {
          Mockito.reset(getPersonsService)
          Mockito.reset(auditService)
          whenever(getPersonsService.execute(firstName, lastName, null, dateOfBirth)).thenReturn(
            Response(
              data =
                listOf(
                  Person(
                    firstName = "Barry",
                    lastName = "Allen",
                    middleName = "Jonas",
                    dateOfBirth = LocalDate.parse("2023-03-01"),
                    contactDetails = ContactDetailsWithEmailAndPhone(phoneNumbers, emails),
                  ),
                  Person(
                    firstName = "Barry",
                    lastName = "Allen",
                    middleName = "Rock",
                    dateOfBirth = LocalDate.parse("2022-07-22"),
                    contactDetails = ContactDetailsWithEmailAndPhone(phoneNumbers, emails),
                  ),
                ),
            ),
          )
        }

        it("gets a person with matching search criteria") {
          mockMvc.performAuthorised("$basePath?first_name=$firstName&last_name=$lastName&pnc_number=$pncNumber&date_of_birth=$dateOfBirth")

          verify(getPersonsService, times(1)).execute(firstName, lastName, pncNumber, dateOfBirth)
        }

        it("gets a person with matching first name") {
          mockMvc.performAuthorised("$basePath?first_name=$firstName")
          verify(getPersonsService, times(1)).execute(firstName, null, null, null)
        }

        it("gets a person with matching last name") {
          mockMvc.performAuthorised("$basePath?last_name=$lastName")
          verify(getPersonsService, times(1)).execute(null, lastName, null, null)
        }

        it("gets a person with matching alias") {
          mockMvc.performAuthorised("$basePath?first_name=$firstName&search_within_aliases=true")
          verify(getPersonsService, times(1)).execute(firstName, null, null, null, searchWithinAliases = true)
        }

        it("gets a person with matching pncNumber") {
          mockMvc.performAuthorised("$basePath?pnc_number=$pncNumber")
          verify(getPersonsService, times(1)).execute(null, null, pncNumber, null)
        }

        it("gets a person with matching date of birth") {
          mockMvc.performAuthorised("$basePath?date_of_birth=$dateOfBirth")

          verify(getPersonsService, times(1)).execute(null, null, null, dateOfBirth)
        }

        it("defaults to not searching within aliases") {
          mockMvc.performAuthorised("$basePath?first_name=$firstName")

          verify(getPersonsService, times(1)).execute(firstName, null, null, null)
        }

        it("logs audit") {
          mockMvc.performAuthorised("$basePath?first_name=$firstName&last_name=$lastName&pnc_number=$pncNumber&date_of_birth=$dateOfBirth")
          verify(
            auditService,
            times(1),
          ).createEvent(
            "SEARCH_PERSON",
            mapOf("firstName" to firstName, "lastName" to lastName, "aliases" to false.toString(), "pncNumber" to pncNumber, "dateOfBirth" to dateOfBirth),
          )
        }

        it("returns paginated results") {
          whenever(getPersonsService.execute(firstName, lastName, null, dateOfBirth)).thenReturn(
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
            mockMvc.performAuthorised(
              "$basePath?first_name=$firstName&last_name=$lastName&date_of_birth=$dateOfBirth&page=3&perPage=5",
            )

          result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 3)
          result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 4)
        }

        it("returns an empty list embedded in a JSON object when no matching people") {
          val firstNameThatDoesNotExist = "Bob21345"
          val lastNameThatDoesNotExist = "Gun36773"

          whenever(getPersonsService.execute(firstNameThatDoesNotExist, lastNameThatDoesNotExist, null, null)).thenReturn(
            Response(
              data = emptyList(),
            ),
          )

          val result = mockMvc.performAuthorised("$basePath?first_name=$firstNameThatDoesNotExist&last_name=$lastNameThatDoesNotExist")

          result.response.contentAsString.shouldContain("\"data\":[]".removeWhitespaceAndNewlines())
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised("$basePath?first_name=$firstName&last_name=$lastName&date_of_birth=$dateOfBirth")

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("returns a 400 BAD REQUEST status code when no search criteria provided") {
          val result = mockMvc.performAuthorised(basePath)

          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
          result.response.contentAsString.shouldContain("No query parameters specified.")
        }

        it("returns a 400 BAD REQUEST status code when no search criteria provided") {
          val result = mockMvc.performAuthorised("$basePath?date_of_birth=12323423234")

          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
          result.response.contentAsString.shouldContain("Invalid date format. Please use yyyy-MM-dd.")
        }
      }

      describe("GET $basePath return Internal Server Error when Upstream api throw unexpected error") {
        beforeTest {
          Mockito.reset(getPersonsService)
          Mockito.reset(auditService)
        }

        it("fails with the appropriate error when an upstream service is down") {
          whenever(getPersonsService.execute(firstName, lastName, pncNumber, dateOfBirth, false)).doThrow(
            WebClientResponseException(500, "MockError", null, null, null, null),
          )

          val response =
            mockMvc.performAuthorised(
              "$basePath?first_name=$firstName&last_name=$lastName&pnc_number=$pncNumber&date_of_birth=$dateOfBirth",
            )

          assert(response.response.status == 500)
          assert(
            response.response.contentAsString.equals(
              "{\"status\":500,\"errorCode\":null,\"userMessage\":\"500 MockError\",\"developerMessage\":\"Unable to complete request as an upstream service is not responding\",\"moreInfo\":null}",
            ),
          )
        }
      }

      describe("GET $basePath/{id}") {
        val probationOffenderSearch = PersonOnProbation(Person("Sam", "Smith", identifiers = Identifiers(nomisNumber = "1234ABC"), currentExclusion = true, exclusionMessage = "An exclusion exists", currentRestriction = false), underActiveSupervision = true)
        val prisonOffenderSearch = POSPrisoner("Kim", "Kardashian")
        val prisonResponse = Response(data = prisonOffenderSearch, errors = emptyList())

        beforeTest {
          Mockito.reset(getPersonService)
          Mockito.reset(auditService)

          val personMap =
            OffenderSearchResponse(
              probationOffenderSearch = probationOffenderSearch,
              prisonerOffenderSearch = prisonResponse.data.toPerson(),
            )

          whenever(getPersonService.getCombinedDataForPerson(hmppsId)).thenReturn(Response(data = personMap))
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId")

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("logs audit") {
          mockMvc.performAuthorised("$basePath/$encodedHmppsId")
          verify(auditService, times(1)).createEvent("GET_PERSON_DETAILS", mapOf("hmppsId" to hmppsId))
        }

        describe("404 Not found") {
          beforeTest {
            Mockito.reset(auditService)
          }
          val idThatDoesNotExist = "9999/11111Z"

          it("returns a 404 status code when a person cannot be found in both upstream APIs") {
            whenever(getPersonService.getCombinedDataForPerson(idThatDoesNotExist)).thenReturn(
              Response(
                data = OffenderSearchResponse(null, null),
                errors =
                  listOf(
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
            whenever(getPersonService.getCombinedDataForPerson(idThatDoesNotExist)).thenReturn(
              Response(
                data = OffenderSearchResponse(prisonerOffenderSearch = null, probationOffenderSearch = PersonOnProbation(Person("someFirstName", "someLastName"), underActiveSupervision = false)),
                errors =
                  listOf(
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

          verify(getPersonService, times(1)).getCombinedDataForPerson(hmppsId)
        }

        it("returns a person with the matching ID") {
          val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId")

          result.response.contentAsString.shouldBe(
            """
            {
             "data":{
                "prisonerOffenderSearch":{
                   "firstName":"Kim",
                   "lastName":"Kardashian",
                   "middleName":null,
                   "dateOfBirth":null,
                   "gender":null,
                   "ethnicity":null,
                   "aliases":[
                   ],
                   "identifiers":{
                      "nomisNumber":null,
                      "croNumber":null,
                      "deliusCrn":null
                   },
                   "pncId":null,
                   "hmppsId":null,
                   "contactDetails":null,
                   "currentRestriction": null,
                   "restrictionMessage": null,
                   "currentExclusion": null,
                   "exclusionMessage": null
                },
                "probationOffenderSearch":{
                   "underActiveSupervision":true,
                   "firstName":"Sam",
                   "lastName":"Smith",
                   "middleName":null,
                   "dateOfBirth":null,
                   "gender":null,
                   "ethnicity":null,
                   "aliases":[
                   ],
                   "identifiers":{
                      "nomisNumber":"1234ABC",
                      "croNumber":null,
                      "deliusCrn":null
                   },
                   "pncId":null,
                   "hmppsId":null,
                   "contactDetails":null,
                   "currentRestriction": null,
                   "restrictionMessage": null,
                   "currentExclusion": null,
                   "exclusionMessage": null
                }
             }
          }
        """.removeWhitespaceAndNewlines(),
          )
        }
      }

      describe("GET $basePath/$encodedHmppsId/name") {

        beforeTest {
          Mockito.reset(getNameForPersonService)
          whenever(getNameForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data = PersonName(firstName = "Sam", lastName = "Smith"),
            ),
          )
          Mockito.reset(auditService)
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId/name")

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("logs audit") {
          mockMvc.performAuthorised("$basePath/$encodedHmppsId/name")
          verify(auditService, times(1)).createEvent("GET_PERSON_NAME", mapOf("hmppsId" to hmppsId))
        }

        describe("404 Not found") {
          beforeTest {
            Mockito.reset(auditService)
          }
          val idThatDoesNotExist = "9999/11111Z"

          it("returns a 404 status code when a person cannot be found in both upstream APIs") {
            whenever(getNameForPersonService.execute(idThatDoesNotExist)).thenReturn(
              Response(
                data = null,
                errors =
                  listOf(
                    UpstreamApiError(
                      causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                      type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                    ),
                  ),
              ),
            )

            val encodedIdThatDoesNotExist = URLEncoder.encode(idThatDoesNotExist, StandardCharsets.UTF_8)
            val result = mockMvc.performAuthorised("$basePath/$encodedIdThatDoesNotExist/name")

            result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
          }
        }

        it("gets a person name details with the matching ID") {
          mockMvc.performAuthorised("$basePath/$encodedHmppsId/name")

          verify(getNameForPersonService, times(1)).execute(hmppsId)
        }

        it("returns person name with the matching ID") {
          val result = mockMvc.performAuthorised("$basePath/$encodedHmppsId/name")

          result.response.contentAsString.shouldBe(
            """
            {
             "data":{
                   "firstName":"Sam",
                   "lastName":"Smith"
             }
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
              data =
                listOf(
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
              data =
                List(20) {
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
          verify(auditService, times(1)).createEvent("GET_PERSON_IMAGE", mapOf("hmppsId" to hmppsId))
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
              errors =
                listOf(
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

      describe("GET $basePath/$sanitisedHmppsId/contacts") {
        beforeTest {
          Mockito.reset(getPrisonerContactsService)
          Mockito.reset(auditService)
          whenever(getPrisonerContactsService.execute(sanitisedHmppsId, page = 1, size = 10, filter = null)).thenReturn(
            Response(
              data =
                PaginatedPrisonerContacts(
                  content =
                    listOf(
                      PrisonerContact(
                        contact =
                          Contact(
                            contactId = 654321L,
                            lastName = "Doe",
                            firstName = "John",
                            middleNames = "William",
                            dateOfBirth = "1980-01-01",
                            flat = "Flat 1",
                            property = "123",
                            street = "Baker Street",
                            area = "Marylebone",
                            cityCode = "25343",
                            cityDescription = "Sheffield",
                            countyCode = "S.YORKSHIRE",
                            countyDescription = "South Yorkshire",
                            postCode = "NW1 6XE",
                            countryCode = "ENG",
                            countryDescription = "England",
                            primaryAddress = true,
                            mailAddress = true,
                            phoneType = "MOB",
                            phoneTypeDescription = "Mobile",
                            phoneNumber = "+1234567890",
                            extNumber = "123",
                          ),
                        relationship =
                          PrisonerContactRelationship(
                            relationshipTypeCode = "FRIEND",
                            relationshipTypeDescription = "Friend",
                            relationshipToPrisonerCode = "FRI",
                            relationshipToPrisonerDescription = "Friend of",
                            approvedVisitor = true,
                            nextOfKin = false,
                            emergencyContact = true,
                            isRelationshipActive = true,
                            currentTerm = true,
                            comments = "Close family friend",
                          ),
                      ),
                    ),
                  isLastPage = true,
                  count = 1,
                  page = 1,
                  perPage = 10,
                  totalCount = 1,
                  totalPages = 1,
                ),
            ),
          )
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/contacts")

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("logs audit") {
          mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/contacts")
          verify(auditService, times(1)).createEvent("GET_PRISONER_CONTACTS", mapOf("hmppsId" to sanitisedHmppsId))
        }

        it("returns a 404 status code when a person cannot be found in both upstream APIs") {
          val idThatDoesNotExist = "blablabla"
          whenever(getPrisonerContactsService.execute(idThatDoesNotExist, page = 1, size = 10, filter = null)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PERSONAL_RELATIONSHIPS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )
          val result = mockMvc.performAuthorised("$basePath/$idThatDoesNotExist/contacts")
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns a 400 status code when a invalid hmppsId") {
          val idThatDoesNotExist = "blablabla"
          whenever(getPrisonerContactsService.execute(idThatDoesNotExist, page = 1, size = 10, filter = null)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NOMIS,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised("$basePath/$idThatDoesNotExist/contacts")

          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }

        it("verify getPrisonerContactsService is called ") {
          mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/contacts")

          verify(getPrisonerContactsService, times(1)).execute(sanitisedHmppsId, page = 1, size = 10, filter = null)
        }

        it("returns prisoner contacts with the matching ID") {
          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/contacts")
          result.response.contentAsString.shouldBe(
            """
            {
             "data":[
                  {
                    "contact": {
                      "contactId": 654321,
                      "lastName": "Doe",
                      "firstName": "John",
                      "middleNames": "William",
                      "dateOfBirth": "1980-01-01",
                      "flat": "Flat 1",
                      "property": "123",
                      "street": "Baker Street",
                      "area": "Marylebone",
                      "cityCode": "25343",
                      "cityDescription": "Sheffield",
                      "countyCode": "S.YORKSHIRE",
                      "countyDescription": "South Yorkshire",
                      "postCode": "NW1 6XE",
                      "countryCode": "ENG",
                      "countryDescription": "England",
                      "primaryAddress": true,
                      "mailAddress": true,
                      "phoneType": "MOB",
                      "phoneTypeDescription": "Mobile",
                      "phoneNumber": "+1234567890",
                      "extNumber": "123"
                    },
                    "relationship": {
                      "relationshipTypeCode": "FRIEND",
                      "relationshipTypeDescription": "Friend",
                      "relationshipToPrisonerCode": "FRI",
                      "relationshipToPrisonerDescription": "Friend of",
                      "approvedVisitor": true,
                      "nextOfKin": false,
                      "emergencyContact": true,
                      "isRelationshipActive": true,
                      "currentTerm": true,
                      "comments": "Close family friend"
                    }
                  }
                ],
                "pagination":{"isLastPage":true,"count":1,"page":1,"perPage":10,"totalCount":1,"totalPages":1}
          }
        """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns many prisoner contacts with the matching ID") {
          whenever(getPrisonerContactsService.execute(sanitisedHmppsId, page = 1, size = 10, filter = null)).thenReturn(
            Response(
              data =
                PaginatedPrisonerContacts(
                  content =
                    listOf(
                      PrisonerContact(
                        contact =
                          Contact(
                            contactId = 654321L,
                            lastName = "Doe",
                            firstName = "John",
                            middleNames = "William",
                            dateOfBirth = "1980-01-01",
                            flat = "Flat 1",
                            property = "123",
                            street = "Baker Street",
                            area = "Marylebone",
                            cityCode = "25343",
                            cityDescription = "Sheffield",
                            countyCode = "S.YORKSHIRE",
                            countyDescription = "South Yorkshire",
                            postCode = "NW1 6XE",
                            countryCode = "ENG",
                            countryDescription = "England",
                            primaryAddress = true,
                            mailAddress = true,
                            phoneType = "MOB",
                            phoneTypeDescription = "Mobile",
                            phoneNumber = "+1234567890",
                            extNumber = "123",
                          ),
                        relationship =
                          PrisonerContactRelationship(
                            relationshipTypeCode = "FRIEND",
                            relationshipTypeDescription = "Friend",
                            relationshipToPrisonerCode = "FRI",
                            relationshipToPrisonerDescription = "Friend of",
                            approvedVisitor = true,
                            nextOfKin = false,
                            emergencyContact = true,
                            isRelationshipActive = true,
                            currentTerm = true,
                            comments = "Close family friend",
                          ),
                      ),
                      PrisonerContact(
                        contact =
                          Contact(
                            contactId = 1234667L,
                            lastName = "Doe",
                            firstName = "BOB",
                            middleNames = "William",
                            dateOfBirth = "1980-01-01",
                            flat = "Flat 1",
                            property = "123",
                            street = "Baker Street",
                            area = "Marylebone",
                            cityCode = "25343",
                            cityDescription = "Sheffield",
                            countyCode = "S.YORKSHIRE",
                            countyDescription = "South Yorkshire",
                            postCode = "NW1 6XE",
                            countryCode = "ENG",
                            countryDescription = "England",
                            primaryAddress = true,
                            mailAddress = true,
                            phoneType = "MOB",
                            phoneTypeDescription = "Mobile",
                            phoneNumber = "+1234567890",
                            extNumber = "123",
                          ),
                        relationship =
                          PrisonerContactRelationship(
                            relationshipTypeCode = "ROOMMATE",
                            relationshipTypeDescription = "Friend",
                            relationshipToPrisonerCode = "FRI",
                            relationshipToPrisonerDescription = "Friend of",
                            approvedVisitor = true,
                            nextOfKin = false,
                            emergencyContact = true,
                            isRelationshipActive = true,
                            currentTerm = true,
                            comments = "Close family friend",
                          ),
                      ),
                    ),
                  isLastPage = true,
                  count = 2,
                  page = 1,
                  perPage = 10,
                  totalCount = 2,
                  totalPages = 1,
                ),
            ),
          )
          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/contacts")
          result.response.contentAsString.shouldBe(
            """
            {"data":[{"contact":{"contactId":654321,"lastName":"Doe","firstName":"John","middleNames":"William","dateOfBirth":"1980-01-01","flat":"Flat 1","property":"123","street":"Baker Street","area":"Marylebone","cityCode":"25343","cityDescription":"Sheffield","countyCode":"S.YORKSHIRE","countyDescription":"South Yorkshire","postCode":"NW1 6XE","countryCode":"ENG","countryDescription":"England","primaryAddress":true,"mailAddress":true,"phoneType":"MOB","phoneTypeDescription":"Mobile","phoneNumber":"+1234567890","extNumber":"123"},"relationship":{"relationshipTypeCode":"FRIEND","relationshipTypeDescription":"Friend","relationshipToPrisonerCode":"FRI","relationshipToPrisonerDescription":"Friend of","approvedVisitor":true,"nextOfKin":false,"emergencyContact":true,"isRelationshipActive":true,"currentTerm":true,"comments":"Close family friend"}},{"contact":{"contactId":1234667,"lastName":"Doe","firstName":"BOB","middleNames":"William","dateOfBirth":"1980-01-01","flat":"Flat 1","property":"123","street":"Baker Street","area":"Marylebone","cityCode":"25343","cityDescription":"Sheffield","countyCode":"S.YORKSHIRE","countyDescription":"South Yorkshire","postCode":"NW1 6XE","countryCode":"ENG","countryDescription":"England","primaryAddress":true,"mailAddress":true,"phoneType":"MOB","phoneTypeDescription":"Mobile","phoneNumber":"+1234567890","extNumber":"123"},"relationship":{"relationshipTypeCode":"ROOMMATE","relationshipTypeDescription":"Friend","relationshipToPrisonerCode":"FRI","relationshipToPrisonerDescription":"Friend of","approvedVisitor":true,"nextOfKin":false,"emergencyContact":true,"isRelationshipActive":true,"currentTerm":true,"comments":"Close family friend"}}],"pagination":{"isLastPage":true,"count":2,"page":1,"perPage":10,"totalCount":2,"totalPages":1}}
        """.removeWhitespaceAndNewlines(),
          )
        }
      }

      describe("GET $basePath/$sanitisedHmppsId/visit-orders") {
        beforeTest {
          Mockito.reset(getPersonsService)
          Mockito.reset(auditService)
          val filters = ConsumerFilters(prisons = emptyList())
          whenever(getPersonService.getNomisNumberWithPrisonFilter(sanitisedHmppsId, filters)).thenReturn(Response(NomisNumber("A1234AA")))
        }

        it("returns a prisoners visit orders") {
          whenever(getVisitOrdersForPersonService.execute(sanitisedHmppsId)).thenReturn(
            Response(
              data =
                VisitOrders(
                  remainingVisitOrders = 10,
                  remainingPrivilegeVisitOrders = 5,
                ),
            ),
          )

          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/visit-orders")
          result.response.contentAsString.shouldBe("""{"data":{"remainingVisitOrders":10,"remainingPrivilegeVisitOrders":5}}""")
        }

        it("returns a 404 when no prisoner visit orders found") {
          whenever(getVisitOrdersForPersonService.execute(sanitisedHmppsId)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NOMIS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )
          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/visit-orders")
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns a 400 when invalid hmppsid") {
          whenever(getVisitOrdersForPersonService.execute(sanitisedHmppsId)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NOMIS,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/visit-orders")
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }
      }
    },
  )
