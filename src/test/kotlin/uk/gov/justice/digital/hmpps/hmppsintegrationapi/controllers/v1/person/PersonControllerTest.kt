package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory.times
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebMvcTestConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.BodyMark
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Contact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.IEPLevel
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Language
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NumberOfChildren
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenderSearchResult
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedPrisonerContacts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonOnProbation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonalCareNeed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PhysicalCharacteristics
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContactRelationship
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerEducation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Qualification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitOrders
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationintegrationepf.LimitedAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.fullAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithPrisonFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCareNeedsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetIEPLevelService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageMetadataForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetLanguagesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetNameForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetNumberOfChildrenForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPhysicalCharacteristicsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonerContactsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonerEducationService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitOrdersForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDateTime
import kotlin.random.Random

@WebMvcTest(controllers = [PersonController::class])
@Import(WebMvcTestConfiguration::class)
@ActiveProfiles("test")
internal class PersonControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val getPersonsService: GetPersonsService,
  @MockitoBean val getNameForPersonService: GetNameForPersonService,
  @MockitoBean val getImageMetadataForPersonService: GetImageMetadataForPersonService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getPrisonerContactsService: GetPrisonerContactsService,
  @MockitoBean val getIEPLevelService: GetIEPLevelService,
  @MockitoBean val getVisitOrdersForPersonService: GetVisitOrdersForPersonService,
  @MockitoBean val getNumberOfChildrenForPersonService: GetNumberOfChildrenForPersonService,
  @MockitoBean val getPhysicalCharacteristicsForPersonService: GetPhysicalCharacteristicsForPersonService,
  @MockitoBean val getCareNeedsForPersonService: GetCareNeedsForPersonService,
  @MockitoBean val getLanguagesForPersonService: GetLanguagesForPersonService,
  @MockitoBean val getPrisonerEducationService: GetPrisonerEducationService,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
) : DescribeSpec(
    {
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val hmppsId = "A1234AA"
      val sanitisedHmppsId = "A1234AA"
      val pncNumber = "2003/13116M"
      val basePath = "/v1/persons"
      val filters = null

      // Test persona
      val person = personInProbationAndNomisPersona
      val firstName = person.firstName
      val lastName = person.lastName
      val dateOfBirth = person.dateOfBirth
      val contactDetails = person.contactDetails
      val identifiers = person.identifiers

      fun notFoundErrors(vararg upstreamApi: UpstreamApi) = upstreamApi.map { UpstreamApiError(causedBy = it, type = UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "MockError") }.toList()

      fun <T> notFoundErrorResponse(vararg upstreamApi: UpstreamApi) = Response<T?>(data = null, errors = notFoundErrors(*upstreamApi))

      fun <T> notFoundErrorResponseEmptyList(vararg upstreamApi: UpstreamApi) = Response<List<T>>(data = emptyList(), errors = notFoundErrors(*upstreamApi))

      beforeTest {
        whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.NORMALISED_PATH_MATCHING)).thenReturn(true)
      }

      describe("GET $basePath") {
        beforeTest {
          Mockito.reset(getPersonsService)
          Mockito.reset(auditService)

          whenever(getPersonsService.personAttributeSearch(firstName, lastName, null, dateOfBirth.toString())).thenReturn(
            Response(
              data =
                listOf(
                  Person(
                    firstName = firstName,
                    lastName = lastName,
                    dateOfBirth = dateOfBirth,
                    contactDetails = contactDetails,
                  ),
                ),
            ),
          )
        }

        afterTest {
          unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
        }

        it("gets a person with matching search criteria") {
          val result = mockMvc.performAuthorised("$basePath?first_name=$firstName&last_name=$lastName&pnc_number=$pncNumber&date_of_birth=$dateOfBirth")
          result.response.status.shouldBe(HttpStatus.OK.value())
          verify(getPersonsService, times(1)).personAttributeSearch(firstName, lastName, pncNumber, dateOfBirth.toString())
        }

        it("gets a person with matching first name") {
          mockMvc.performAuthorised("$basePath?first_name=$firstName")
          verify(getPersonsService, times(1)).personAttributeSearch(firstName, null, null, null)
        }

        it("gets a person with matching last name") {
          mockMvc.performAuthorised("$basePath?last_name=$lastName")
          verify(getPersonsService, times(1)).personAttributeSearch(null, lastName, null, null)
        }

        it("gets a person with matching alias") {
          mockMvc.performAuthorised("$basePath?first_name=$firstName&search_within_aliases=true")
          verify(getPersonsService, times(1)).personAttributeSearch(firstName, null, null, null, searchWithinAliases = true)
        }

        it("gets a person with matching pncNumber") {
          mockMvc.performAuthorised("$basePath?pnc_number=$pncNumber")
          verify(getPersonsService, times(1)).personAttributeSearch(null, null, pncNumber, null)
        }

        it("gets a person with matching date of birth") {
          mockMvc.performAuthorised("$basePath?date_of_birth=$dateOfBirth")
          verify(getPersonsService, times(1)).personAttributeSearch(null, null, null, dateOfBirth.toString())
        }

        it("defaults to not searching within aliases") {
          mockMvc.performAuthorised("$basePath?first_name=$firstName")
          verify(getPersonsService, times(1)).personAttributeSearch(firstName, null, null, null)
        }

        it("calls attribute search when prisons filter is present and pnc number in search") {
          mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
          every { roles[any()] } returns testRoleWithPrisonFilters
          mockMvc.performAuthorised("$basePath?first_name=$firstName&pnc_number=$pncNumber")
          verify(getPersonsService, times(1)).personAttributeSearch(firstName, null, pncNumber, null, consumerFilters = testRoleWithPrisonFilters.filters)
        }

        it("calls attribute search when prisons filter is present and pnc number in search") {
          mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
          every { roles[any()] } returns testRoleWithPrisonFilters
          mockMvc.performAuthorised("$basePath?first_name=$firstName&pnc_number=$pncNumber")
          verify(getPersonsService, times(1)).personAttributeSearch(firstName, null, pncNumber, null, consumerFilters = testRoleWithPrisonFilters.filters)
        }

        it("passes supervision status filters from consumer config to service") {
          mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
          every { roles[any()] } returns role("test-role") { permissions { -fullAccess.permissions!! } }
          val expectedFilters = ConsumerFilters(supervisionStatuses = listOf("PRISONS"))
          whenever(getPersonsService.personAttributeSearch(firstName, null, pncNumber, null, false, expectedFilters)).thenReturn(Response(data = emptyList()))

          mockMvc.performAuthorisedWithCN("$basePath?first_name=$firstName&pnc_number=$pncNumber", "supervision-status-prison-only")

          verify(getPersonsService, times(1)).personAttributeSearch(firstName, null, pncNumber, null, false, expectedFilters)
        }

        it("gets a person with matching search criteria") {
          mockMvc.performAuthorised("$basePath?first_name=$firstName&last_name=$lastName&pnc_number=$pncNumber&date_of_birth=$dateOfBirth")
          verify(getPersonsService, times(1)).personAttributeSearch(firstName, lastName, pncNumber, dateOfBirth.toString())
        }

        it("gets a person with matching first name") {
          mockMvc.performAuthorised("$basePath?first_name=$firstName")
          verify(getPersonsService, times(1)).personAttributeSearch(firstName, null, null, null)
        }

        it("gets a person with matching last name") {
          mockMvc.performAuthorised("$basePath?last_name=$lastName")
          verify(getPersonsService, times(1)).personAttributeSearch(null, lastName, null, null)
        }

        it("gets a person with matching alias") {
          mockMvc.performAuthorised("$basePath?first_name=$firstName&search_within_aliases=true")
          verify(getPersonsService, times(1)).personAttributeSearch(firstName, null, null, null, searchWithinAliases = true)
        }

        it("gets a person with matching pncNumber") {
          mockMvc.performAuthorised("$basePath?pnc_number=$pncNumber")
          verify(getPersonsService, times(1)).personAttributeSearch(null, null, pncNumber, null)
        }

        it("gets a person with matching date of birth") {
          mockMvc.performAuthorised("$basePath?date_of_birth=$dateOfBirth")
          verify(getPersonsService, times(1)).personAttributeSearch(null, null, null, dateOfBirth.toString())
        }

        it("defaults to not searching within aliases") {
          mockMvc.performAuthorised("$basePath?first_name=$firstName")
          verify(getPersonsService, times(1)).personAttributeSearch(firstName, null, null, null)
        }

        it("logs audit") {
          mockMvc.performAuthorised("$basePath?first_name=$firstName&last_name=$lastName&pnc_number=$pncNumber&date_of_birth=$dateOfBirth")
          verify(
            auditService,
            times(1),
          ).createEvent(
            "SEARCH_PERSON",
            mapOf("firstName" to firstName, "lastName" to lastName, "aliases" to false.toString(), "pncNumber" to pncNumber, "dateOfBirth" to dateOfBirth.toString()),
          )
        }

        it("returns paginated results") {
          whenever(getPersonsService.personAttributeSearch(firstName, lastName, null, dateOfBirth.toString())).thenReturn(
            Response(
              data =
                List(20) { i ->
                  Person(
                    firstName = "${person.firstName} $i",
                    lastName = "${person.firstName} $i",
                    dateOfBirth = dateOfBirth,
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

          whenever(getPersonsService.personAttributeSearch(firstNameThatDoesNotExist, lastNameThatDoesNotExist, null, null)).thenReturn(
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
          whenever(getPersonsService.personAttributeSearch(firstName, lastName, pncNumber, dateOfBirth.toString(), false)).doThrow(
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
        val probationOffenderSearch = PersonOnProbation(Person(firstName, lastName, identifiers = identifiers, currentExclusion = true, exclusionMessage = "An exclusion exists", currentRestriction = false), underActiveSupervision = true)
        val prisonOffenderSearch = POSPrisoner(firstName = firstName, lastName = lastName, youthOffender = false, prisonerNumber = identifiers.nomisNumber)
        val prisonResponse = Response(data = prisonOffenderSearch, errors = emptyList())

        beforeTest {
          Mockito.reset(getPersonService)
          Mockito.reset(auditService)

          val personMap =
            OffenderSearchResult(
              probationOffenderSearch = probationOffenderSearch,
              prisonerOffenderSearch = prisonResponse.data.toPerson(),
            )
          whenever(getPersonService.getCombinedDataForPerson(hmppsId)).thenReturn(Response(data = personMap))
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised("$basePath/$hmppsId")
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("logs audit") {
          mockMvc.performAuthorised("$basePath/$hmppsId")
          verify(auditService, times(1)).createEvent("GET_PERSON_DETAILS", mapOf("hmppsId" to hmppsId))
        }

        describe("404 Not found") {
          val idThatDoesNotExist = "A4321AA"

          beforeTest {
            Mockito.reset(auditService)
          }

          it("returns a 404 status code when a person cannot be found in both upstream APIs") {
            whenever(getPersonService.getCombinedDataForPerson(idThatDoesNotExist))
              .thenReturn(notFoundErrorResponse(UpstreamApi.NDELIUS))

            val result = mockMvc.performAuthorised("$basePath/$idThatDoesNotExist")
            result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
          }

          it("does not return a 404 status code when a person was found in one upstream API") {
            whenever(getPersonService.getCombinedDataForPerson(idThatDoesNotExist)).thenReturn(
              Response(
                data = OffenderSearchResult(prisonerOffenderSearch = null, probationOffenderSearch = PersonOnProbation(Person("someFirstName", "someLastName"), underActiveSupervision = false)),
                errors = notFoundErrors(UpstreamApi.PRISONER_OFFENDER_SEARCH),
              ),
            )
            val result = mockMvc.performAuthorised("$basePath/$idThatDoesNotExist")
            result.response.status.shouldNotBe(HttpStatus.NOT_FOUND.value())
          }
        }

        it("gets a person with the matching ID") {
          mockMvc.performAuthorised("$basePath/$hmppsId")
          verify(getPersonService, times(1)).getCombinedDataForPerson(hmppsId)
        }

        it("returns a person with the matching ID") {
          val result = mockMvc.performAuthorised("$basePath/$hmppsId")
          result.response.contentAsString.shouldBe(
            """
             {
               "data": {
                  "prisonerOffenderSearch":{
                     "firstName": "${person.firstName}",
                     "lastName": "${person.lastName}",
                     "middleName": null,
                     "dateOfBirth": null,
                     "gender": null,
                     "ethnicity": null,
                     "aliases": [],
                     "identifiers":{
                        "nomisNumber": "${person.identifiers.nomisNumber}",
                        "croNumber": null,
                        "deliusCrn": null
                     },
                     "pncId": null,
                     "hmppsId": "${person.identifiers.nomisNumber}",
                     "contactDetails": null,
                     "currentRestriction": null,
                     "restrictionMessage": null,
                     "currentExclusion": null,
                     "exclusionMessage": null
                  },
                  "probationOffenderSearch": {
                     "underActiveSupervision": true,
                     "firstName": "${person.firstName}",
                     "lastName": "${person.lastName}",
                     "middleName": null,
                     "dateOfBirth": null,
                     "gender": null,
                     "ethnicity": null,
                     "aliases": [],
                     "identifiers": {
                        "nomisNumber": "${person.identifiers.nomisNumber}",
                        "croNumber": null,
                        "deliusCrn": "${person.identifiers.deliusCrn}"
                     },
                     "pncId": null,
                     "hmppsId": null,
                     "contactDetails": null,
                     "currentRestriction": false,
                     "restrictionMessage": null,
                     "currentExclusion": true,
                     "exclusionMessage": "An exclusion exists"
                  }
               }
            }
            """.removeWhitespaceAndNewlines(),
          )
        }
      }

      describe("GET $basePath/$sanitisedHmppsId/name") {
        beforeTest {
          Mockito.reset(getNameForPersonService)
          Mockito.reset(auditService)

          whenever(getNameForPersonService.execute(sanitisedHmppsId, filters)).thenReturn(
            Response(
              data = PersonName(firstName, lastName),
            ),
          )
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/name")
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("logs audit") {
          mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/name")
          verify(auditService, times(1)).createEvent("GET_PERSON_NAME", mapOf("hmppsId" to sanitisedHmppsId))
        }

        describe("404 Not found") {
          val idThatDoesNotExist = "B5678BB"

          beforeTest {
            Mockito.reset(auditService)
          }

          it("returns a 404 status code when a person cannot be found in both upstream APIs") {
            whenever(getNameForPersonService.execute(idThatDoesNotExist, filters))
              .thenReturn(notFoundErrorResponse(UpstreamApi.NDELIUS))

            val result = mockMvc.performAuthorised("$basePath/$idThatDoesNotExist/name")
            result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
          }
        }

        it("returns a 400 status code when bad request in upstream APIs") {
          whenever(getNameForPersonService.execute(sanitisedHmppsId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISON_API,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/name")
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }

        it("gets a person name details with the matching ID") {
          mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/name")
          verify(getNameForPersonService, times(1)).execute(sanitisedHmppsId, filters)
        }

        it("returns person name with the matching ID") {
          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/name")
          result.response.contentAsString.shouldBe(
            """
            {
               "data":{
                  "firstName": "$firstName",
                  "lastName": "$lastName"
               }
            }
            """.removeWhitespaceAndNewlines(),
          )
        }
      }

      describe("GET $basePath/$sanitisedHmppsId/access-limitations") {
        val path = "$basePath/$sanitisedHmppsId/access-limitations"
        beforeTest {
          Mockito.reset(auditService)
          Mockito.reset(getImageMetadataForPersonService)
          val limitedAccess =
            LimitedAccess(
              excludedFrom = listOf(LimitedAccess.AccessLimitation("someone@justice.gov.uk")),
              exclusionMessage = "You are excluded from viewing this case. Please contact someone for more information.",
              restrictedTo = listOf(),
              restrictionMessage = null,
            )
          whenever(getPersonService.getAccessLimitations(sanitisedHmppsId)).thenReturn(Response(limitedAccess))
        }

        it("logs audit") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          verify(auditService, times(1)).createEvent("GET_LIMITED_ACCESS_INFORMATION", mapOf("hmppsId" to sanitisedHmppsId))
        }

        it("returns limited access information correctly") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsString.shouldBe(
            """
            {
              "data": {
                "excludedFrom": [ {"email":"someone@justice.gov.uk"} ],
                "exclusionMessage": "You are excluded from viewing this case. Please contact someone for more information.",
                "restrictedTo": [],
                "restrictionMessage": null
              },
              "errors":[]
            }
        """.removeWhitespaceAndNewlines(),
          )
        }
      }

      describe("GET $basePath/$sanitisedHmppsId/images") {
        beforeTest {
          Mockito.reset(auditService)
          Mockito.reset(getImageMetadataForPersonService)
          whenever(getImageMetadataForPersonService.execute(sanitisedHmppsId, filters)).thenReturn(
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
          whenever(getImageMetadataForPersonService.execute(sanitisedHmppsId, filters)).thenReturn(
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

          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/images?page=3&perPage=5")
          result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 3)
          result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 4)
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/images")
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("logs audit") {
          mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/images")
          verify(auditService, times(1)).createEvent("GET_PERSON_IMAGE", mapOf("hmppsId" to sanitisedHmppsId))
        }

        it("gets the metadata of images for a person with the matching ID") {
          mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/images")
          verify(getImageMetadataForPersonService, times(1)).execute(sanitisedHmppsId, filters)
        }

        it("returns the metadata of images for a person with the matching ID") {
          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/images")
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
          whenever(getImageMetadataForPersonService.execute(sanitisedHmppsId, filters))
            .thenReturn(notFoundErrorResponseEmptyList(UpstreamApi.PRISON_API))

          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/images")
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
          whenever(getPrisonerContactsService.execute(idThatDoesNotExist, page = 1, size = 10, filter = null))
            .thenReturn(notFoundErrorResponse(UpstreamApi.PERSONAL_RELATIONSHIPS))

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
                    causedBy = UpstreamApi.PRISON_API,
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
              "pagination": {
                "isLastPage": true,
                "count": 1,
                "page": 1,
                "perPage": 10,
                "totalCount": 1,
                "totalPages": 1
              }
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
              {
                "data": [
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
                  },
                  {
                    "contact": {
                      "contactId": 1234667,
                      "lastName": "Doe",
                      "firstName": "BOB",
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
                      "relationshipTypeCode": "ROOMMATE",
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
                "pagination": {
                  "isLastPage": true,
                  "count": 2,
                  "page": 1,
                  "perPage": 10,
                  "totalCount": 2,
                  "totalPages": 1
                }
              }
            """.removeWhitespaceAndNewlines(),
          )
        }
      }

      describe("/v1/persons/{hmppsId}/iep-level") {
        val path = "$basePath/$sanitisedHmppsId/iep-level"
        val iepLevel = IEPLevel(iepCode = "STD", iepLevel = "Standard")

        beforeTest {
          Mockito.reset(getPrisonerContactsService)
          Mockito.reset(auditService)

          whenever(getIEPLevelService.execute(sanitisedHmppsId, filter = null)).thenReturn(
            Response(
              data = iepLevel,
            ),
          )
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)
          verify(auditService, times(1)).createEvent("GET_PRISONER_IEP_LEVEL", mapOf("hmppsId" to sanitisedHmppsId))
        }

        it("returns a 200 OK status code with data") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsString.shouldBe(
            """
            {
              "data": {
                "iepCode": "STD",
                "iepLevel": "Standard"
              }
            }
            """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns a 400 bad request") {
          whenever(getIEPLevelService.execute(sanitisedHmppsId, filter = null)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISON_API,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }

        it("returns a 404 not found") {
          whenever(getIEPLevelService.execute(sanitisedHmppsId, filter = null))
            .thenReturn(notFoundErrorResponse(UpstreamApi.PRISON_API))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }
      }

      describe("GET /v1/persons/{hmppsId}/visit-orders") {
        beforeTest {
          Mockito.reset(getPersonsService)
          Mockito.reset(auditService)

          val filters = ConsumerFilters(prisons = emptyList())
          whenever(getPersonService.getNomisNumber(sanitisedHmppsId, filters)).thenReturn(Response(NomisNumber("A1234AA")))
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
          whenever(getVisitOrdersForPersonService.execute(sanitisedHmppsId))
            .thenReturn(notFoundErrorResponse(UpstreamApi.PRISON_API))

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
                    causedBy = UpstreamApi.PRISON_API,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/visit-orders")
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }
      }

      describe("/v1/persons/{hmppsId}/physical-characteristics") {
        val path = "$basePath/$sanitisedHmppsId/physical-characteristics"
        val physicalCharacteristics =
          PhysicalCharacteristics(
            heightCentimetres = 200,
            weightKilograms = 102,
            hairColour = "Blonde",
            rightEyeColour = "Green",
            leftEyeColour = "Hazel",
            facialHair = "Clean Shaven",
            shapeOfFace = "Round",
            build = "Muscular",
            shoeSize = 10,
            tattoos =
              listOf(
                BodyMark(bodyPart = "Head", comment = "Skull and crossbones covering chest"),
              ),
            scars =
              listOf(
                BodyMark(bodyPart = "Head", comment = "Skull and crossbones covering chest"),
              ),
            marks =
              listOf(
                BodyMark(bodyPart = "Head", comment = "Skull and crossbones covering chest"),
              ),
          )

        beforeTest {
          Mockito.reset(getPhysicalCharacteristicsForPersonService)
          Mockito.reset(auditService)

          whenever(getPhysicalCharacteristicsForPersonService.execute(sanitisedHmppsId, filters)).thenReturn(
            Response(
              data = physicalCharacteristics,
            ),
          )
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)
          verify(auditService, times(1)).createEvent("GET_PERSON_PHYSICAL_CHARACTERISTICS", mapOf("hmppsId" to sanitisedHmppsId))
        }

        it("returns a 200 OK status code with data") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsString.shouldBe(
            """
            {
              "data":{
                "heightCentimetres": 200,
                "weightKilograms": 102,
                "hairColour": "Blonde",
                "rightEyeColour": "Green",
                "leftEyeColour": "Hazel",
                "facialHair": "Clean Shaven",
                "shapeOfFace": "Round",
                "build": "Muscular",
                "shoeSize": 10,
                "tattoos": [
                  {
                    "bodyPart": "Head",
                    "comment": "Skull and crossbones covering chest"
                  }
                ],
                "scars": [
                  {
                    "bodyPart": "Head",
                    "comment": "Skull and crossbones covering chest"
                  }
                ],
                "marks": [
                  {
                    "bodyPart": "Head",
                    "comment": "Skull and crossbones covering chest"
                  }
                ]
              }
            }

          """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns a 400 bad request") {
          whenever(getPhysicalCharacteristicsForPersonService.execute(sanitisedHmppsId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }

        it("returns a 404 not found") {
          whenever(getPhysicalCharacteristicsForPersonService.execute(sanitisedHmppsId, filters))
            .thenReturn(notFoundErrorResponse(UpstreamApi.PRISONER_OFFENDER_SEARCH))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }
      }

      describe("/v1/persons/{hmppsId}/number-of-children") {
        val path = "$basePath/$sanitisedHmppsId/number-of-children"
        val numberOfChildren = NumberOfChildren(numberOfChildren = "2")

        beforeTest {
          Mockito.reset(getNumberOfChildrenForPersonService)
          Mockito.reset(auditService)

          whenever(getNumberOfChildrenForPersonService.execute(sanitisedHmppsId, filters)).thenReturn(
            Response(
              data = numberOfChildren,
            ),
          )
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)
          verify(auditService, times(1)).createEvent("GET_PRISONER_NUMBER_OF_CHILDREN", mapOf("hmppsId" to sanitisedHmppsId))
        }

        it("returns a 200 OK status code with data") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsString.shouldBe(
            """
          {
            "data": {
              "numberOfChildren": "2"
            }
          }
          """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns a 400 bad request") {
          whenever(getNumberOfChildrenForPersonService.execute(sanitisedHmppsId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PERSONAL_RELATIONSHIPS,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }

        it("returns a 404 not found") {
          whenever(getNumberOfChildrenForPersonService.execute(sanitisedHmppsId, filters))
            .thenReturn(notFoundErrorResponse(UpstreamApi.PERSONAL_RELATIONSHIPS))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }
      }

      describe("GET $basePath/$sanitisedHmppsId/visit-orders") {
        beforeTest {
          Mockito.reset(getPersonsService)
          Mockito.reset(auditService)

          val filters = ConsumerFilters(prisons = emptyList())
          whenever(getPersonService.getNomisNumber(sanitisedHmppsId, filters)).thenReturn(Response(NomisNumber("A1234AA")))
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
          whenever(getVisitOrdersForPersonService.execute(sanitisedHmppsId))
            .thenReturn(notFoundErrorResponse(UpstreamApi.PRISON_API))

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
                    causedBy = UpstreamApi.PRISON_API,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/visit-orders")
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }
      }

      describe("/v1/persons/{hmppsId}/care-needs") {
        val path = "$basePath/$sanitisedHmppsId/care-needs"
        val careNeeds =
          listOf(
            PersonalCareNeed(
              problemType = "MATSTAT",
              problemCode = "ACCU9",
              problemStatus = "ON",
              problemDescription = "No Disability",
              commentText = "COMMENT",
              startDate = "2020-06-21",
              endDate = null,
            ),
          )

        beforeTest {
          Mockito.reset(getCareNeedsForPersonService)
          Mockito.reset(auditService)

          whenever(getCareNeedsForPersonService.execute(sanitisedHmppsId, filters)).thenReturn(
            Response(
              data = careNeeds,
            ),
          )
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)
          verify(auditService, times(1)).createEvent("GET_PERSON_CARE_NEEDS", mapOf("hmppsId" to sanitisedHmppsId))
        }

        it("returns a 200 OK status code with data") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsString.shouldBe(
            """
            {
              "data": [
                {
                  "problemType": "MATSTAT",
                  "problemCode": "ACCU9",
                  "problemStatus": "ON",
                  "problemDescription": "No Disability",
                  "commentText": "COMMENT",
                  "startDate": "2020-06-21",
                  "endDate": null
                }
              ]
            }
          """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns a 400 bad request") {
          whenever(getCareNeedsForPersonService.execute(sanitisedHmppsId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }

        it("returns a 404 not found") {
          whenever(getCareNeedsForPersonService.execute(sanitisedHmppsId, filters))
            .thenReturn(notFoundErrorResponse(UpstreamApi.PRISONER_OFFENDER_SEARCH))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }
      }

      describe("/v1/persons/{hmppsId}/languages") {
        val path = "$basePath/$sanitisedHmppsId/languages"
        val languages =
          listOf(
            Language(
              type = "PRIM",
              code = "ENG",
              readSkill = "Y",
              writeSkill = "Y",
              speakSkill = "Y",
              interpreterRequested = true,
            ),
          )

        beforeTest {
          Mockito.reset(getLanguagesForPersonService)
          Mockito.reset(auditService)

          whenever(getLanguagesForPersonService.execute(sanitisedHmppsId, filters)).thenReturn(
            Response(
              data = languages,
            ),
          )
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)
          verify(auditService, times(1)).createEvent("GET_LANGUAGES", mapOf("hmppsId" to sanitisedHmppsId))
        }

        it("returns a 200 OK status code with data") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsString.shouldBe(
            """
            {
              "data": [
                {
                  "type": "PRIM",
                  "code": "ENG",
                  "readSkill": "Y",
                  "writeSkill": "Y",
                  "speakSkill": "Y",
                  "interpreterRequested": true
                }
              ]
            }
        """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns a 400 bad request") {
          whenever(getLanguagesForPersonService.execute(sanitisedHmppsId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }

        it("returns a 404 not found") {
          whenever(getLanguagesForPersonService.execute(sanitisedHmppsId, filters))
            .thenReturn(notFoundErrorResponse(UpstreamApi.PRISONER_OFFENDER_SEARCH))
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }
      }

      describe("/v1/persons/{hmppsId}/education") {
        val path = "$basePath/$sanitisedHmppsId/education"
        val education =
          PrisonerEducation(
            educationLevel = "PRIMARY_SCHOOL",
            qualifications =
              listOf(
                Qualification(
                  subject = "Maths GCSE",
                  level = "ENTRY_LEVEL",
                  grade = "Distinction",
                ),
              ),
          )

        beforeTest {
          Mockito.reset(getLanguagesForPersonService)
          Mockito.reset(auditService)

          whenever(getPrisonerEducationService.execute(sanitisedHmppsId, filters)).thenReturn(
            Response(
              data = education,
            ),
          )
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)
          verify(auditService, times(1)).createEvent("GET_PRISONER_EDUCATION", mapOf("hmppsId" to sanitisedHmppsId))
        }

        it("returns a 200 OK status code with data") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsString.shouldBe(
            """
            {
              "data": {
              "educationLevel": "PRIMARY_SCHOOL",
              "qualifications": [
                {
                  "subject": "Maths GCSE",
                  "level": "ENTRY_LEVEL",
                  "grade": "Distinction"
                }
              ]
            }
            }
        """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns a 400 bad request") {
          whenever(getPrisonerEducationService.execute(sanitisedHmppsId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }

        it("returns a 404 not found") {
          whenever(getPrisonerEducationService.execute(sanitisedHmppsId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )
          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }
      }
    },
  )
