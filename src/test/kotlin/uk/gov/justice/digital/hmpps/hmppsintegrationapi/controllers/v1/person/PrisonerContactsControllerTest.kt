package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebMvcTestConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Contact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedPrisonerContacts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContactRelationship
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonerContactsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [PrisonerContactsController::class])
@Import(WebMvcTestConfiguration::class)
@ActiveProfiles("test")
internal class PrisonerContactsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getPrisonerContactsService: GetPrisonerContactsService,
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

      fun notFoundErrors(vararg upstreamApi: UpstreamApi) = upstreamApi.map { UpstreamApiError(causedBy = it, type = UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "MockError") }.toList()

      fun <T> notFoundErrorResponse(vararg upstreamApi: UpstreamApi) = Response<T?>(data = null, errors = notFoundErrors(*upstreamApi))

      beforeTest {
        whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.NORMALISED_PATH_MATCHING)).thenReturn(true)
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

      describe("GET $basePath/$sanitisedHmppsId/emergency-contacts") {
        beforeTest {
          Mockito.reset(getPrisonerContactsService)
          Mockito.reset(auditService)

          whenever(getPrisonerContactsService.execute(sanitisedHmppsId, page = 1, size = 10, filter = null, emergencyNextOfKinOnly = true)).thenReturn(
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
          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/emergency-contacts")
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("logs audit") {
          mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/emergency-contacts")
          verify(auditService, times(1)).createEvent("GET_PRISONER_EMERGENCY_CONTACTS", mapOf("hmppsId" to sanitisedHmppsId))
        }

        it("returns a 404 status code when a person cannot be found in both upstream APIs") {
          val idThatDoesNotExist = "blablabla"
          whenever(getPrisonerContactsService.execute(idThatDoesNotExist, page = 1, size = 10, filter = null, emergencyNextOfKinOnly = true))
            .thenReturn(notFoundErrorResponse(UpstreamApi.PERSONAL_RELATIONSHIPS))

          val result = mockMvc.performAuthorised("$basePath/$idThatDoesNotExist/emergency-contacts")
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns a 400 status code when a invalid hmppsId") {
          val idThatDoesNotExist = "blablabla"
          whenever(getPrisonerContactsService.execute(idThatDoesNotExist, page = 1, size = 10, filter = null, emergencyNextOfKinOnly = true)).thenReturn(
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

          val result = mockMvc.performAuthorised("$basePath/$idThatDoesNotExist/emergency-contacts")
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }

        it("verify getPrisonerContactsService is called ") {
          mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/emergency-contacts")
          verify(getPrisonerContactsService, times(1)).execute(sanitisedHmppsId, page = 1, size = 10, filter = null, emergencyNextOfKinOnly = true)
        }

        it("returns prisoner contacts with the matching ID") {
          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/emergency-contacts")
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
          whenever(getPrisonerContactsService.execute(sanitisedHmppsId, page = 1, size = 10, filter = null, emergencyNextOfKinOnly = true)).thenReturn(
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

          val result = mockMvc.performAuthorised("$basePath/$sanitisedHmppsId/emergency-contacts")
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
    },
  )
