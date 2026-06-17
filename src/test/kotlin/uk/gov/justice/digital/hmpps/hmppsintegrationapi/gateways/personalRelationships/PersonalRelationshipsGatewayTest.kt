package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.personalRelationships

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PersonalRelationshipsGateway::class],
)
class PersonalRelationshipsGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val personalRelationshipsGateway: PersonalRelationshipsGateway,
) : DescribeSpec({
    val contactId: Long = 123456
    val prisonerContactId: Long = 234561
    val prisonerId = "A1234BC"
    val getChildrenPath = "/prisoner/$prisonerId/number-of-children"
    val personalRelationshipsApiMockServer = ApiMockServer.create(UpstreamApi.PERSONAL_RELATIONSHIPS)

    beforeEach {
      personalRelationshipsApiMockServer.start()
      Mockito.reset(hmppsAuthGateway)

      whenever(hmppsAuthGateway.getClientToken("PERSONAL-RELATIONSHIPS")).thenReturn(HmppsAuthMockServer.TOKEN)
      whenever(hmppsAuthGateway.getClientToken(eq("PERSONAL-RELATIONSHIPS"), any<RequestContext>())).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      personalRelationshipsApiMockServer.stop()
    }
    // "authenticates using HMPPS Auth with credentials"
    it("authenticates using HMPPS Auth with credentials for linked prisoners api") {
      personalRelationshipsGateway.getLinkedPrisoner(contactId)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("PERSONAL-RELATIONSHIPS")
    }

    it("authenticates using HMPPS Auth with credentials for prisoner contact restrictions api") {
      personalRelationshipsGateway.getPrisonerContactRestrictions(prisonerContactId)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("PERSONAL-RELATIONSHIPS")
    }

    it("gets a list of prisoner contact ids") {
      val path = "/contact/$contactId/linked-prisoners"
      personalRelationshipsApiMockServer.stubForGet(
        path,
        body =
          """
          {
            "content": [
              {
                "prisonerNumber": "A1234BC",
                "lastName": "Doe",
                "firstName": "John",
                "middleNames": "William",
                "prisonerContactId": 123456,
                "relationshipTypeCode": "S",
                "relationshipTypeDescription": "Official",
                "relationshipToPrisonerCode": "FRI",
                "relationshipToPrisonerDescription": "Friend",
                "isRelationshipActive": true
              }
            ],
            "page": {
              "size": 10,
              "totalElements": 1,
              "totalPages": 1,
              "number": 0
            }
          }
          """.trimIndent(),
      )

      val response = personalRelationshipsGateway.getLinkedPrisoner(contactId)
      response.errors.shouldBeEmpty()
      response.data.shouldNotBeNull()
      response.data!!
        .prisoners
        .first()
        .prisonerContactId
        .shouldBe(123456)
    }

    // "Get the restrictions that apply for a particular relationship."
    it("Get the restrictions that apply for a particular relationship") {
      val path = "/prisoner-contact/$prisonerContactId/restriction"

      personalRelationshipsApiMockServer.stubForGet(
        path,
        body =
          """
          {
            "prisonerContactRestrictions": [
              {
                "prisonerContactRestrictionId": 123456,
                "prisonerContactId": 123456,
                "contactId": 123456,
                "prisonerNumber": "A1234BC",
                "restrictionType": "BAN",
                "restrictionTypeDescription": "Banned",
                "startDate": "2024-01-01",
                "expiryDate": "2024-01-01",
                "comments": "N/A",
                "enteredByUsername": "admin",
                "enteredByDisplayName": "John Smith",
                "createdBy": "admin",
                "createdTime": "2023-09-23T10:15:30",
                "updatedBy": "admin2",
                "updatedTime": "2023-09-24T12:00:00"
              }
            ],
            "contactGlobalRestrictions": [
              {
                "contactRestrictionId": 1,
                "contactId": 123,
                "restrictionType": "BAN",
                "restrictionTypeDescription": "Banned",
                "startDate": "2024-01-01",
                "expiryDate": "2024-01-01",
                "comments": "N/A",
                "enteredByUsername": "admin",
                "enteredByDisplayName": "John Smith",
                "createdBy": "admin",
                "createdTime": "2023-09-23T10:15:30",
                "updatedBy": "admin2",
                "updatedTime": "2023-09-24T12:00:00"
              }
            ]
          }
          """.trimIndent(),
      )

      val response = personalRelationshipsGateway.getPrisonerContactRestrictions(prisonerContactId)
      response.errors.shouldBeEmpty()
      response.data.shouldNotBeNull()
      response.data!!
        .prisonerContactRestrictions!!
        .first()
        .prisonerContactRestrictionId
        .shouldBe(123456)
    }

    describe("GET /contact/{contactId}") {

      it("authenticates using HMPPS Auth with credentials for linked prisoners api") {

        personalRelationshipsGateway.getContactByContactId(contactId)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("PERSONAL-RELATIONSHIPS")
      }

      it("Gets a contact by id successfully") {
        val path = "/contact/$contactId"
        personalRelationshipsApiMockServer.stubForGet(
          path,
          body =
            """
            {
              "id": 123456,
              "titleCode": "MR",
              "titleDescription": "Mr",
              "lastName": "Doe",
              "firstName": "John",
              "middleNames": "William",
              "dateOfBirth": "1980-01-01",
              "isStaff": false,
              "deceasedDate": "1980-01-01",
              "languageCode": "ENG",
              "languageDescription": "English",
              "interpreterRequired": true,
              "addresses": [
                {
                  "contactAddressId": 123456,
                  "contactId": 123456,
                  "addressType": "HOME",
                  "addressTypeDescription": "HOME",
                  "primaryAddress": true,
                  "flat": "Flat 2B",
                  "property": "Mansion House",
                  "street": "Acacia Avenue",
                  "area": "Morton Heights",
                  "cityCode": "25343",
                  "cityDescription": "Sheffield",
                  "countyCode": "S.YORKSHIRE",
                  "countyDescription": "South Yorkshire",
                  "postcode": "S13 4FH",
                  "countryCode": "ENG",
                  "countryDescription": "England",
                  "verified": false,
                  "verifiedBy": "NJKG44D",
                  "verifiedTime": "2024-01-01T00:00:00Z",
                  "mailFlag": false,
                  "startDate": "2024-01-01",
                  "endDate": "2024-01-01",
                  "noFixedAddress": false,
                  "comments": "Some additional information",
                  "phoneNumbers": [
                    {
                      "contactAddressPhoneId": 1,
                      "contactPhoneId": 1,
                      "contactAddressId": 1,
                      "contactId": 123,
                      "phoneType": "MOB",
                      "phoneTypeDescription": "Mobile phone",
                      "phoneNumber": "+1234567890",
                      "extNumber": "123",
                      "createdBy": "admin",
                      "createdTime": "2023-09-23T10:15:30",
                      "updatedBy": "admin2",
                      "updatedTime": "2023-09-24T12:00:00"
                    }
                  ],
                  "createdBy": "JD000001",
                  "createdTime": "2024-01-01T00:00:00Z",
                  "updatedBy": "JD000001",
                  "updatedTime": "2024-01-01T00:00:00Z"
                }
              ],
              "phoneNumbers": [
                {
                  "contactPhoneId": 1,
                  "contactId": 123,
                  "phoneType": "MOB",
                  "phoneTypeDescription": "Mobile",
                  "phoneNumber": "+1234567890",
                  "extNumber": "123",
                  "createdBy": "admin",
                  "createdTime": "2023-09-23T10:15:30",
                  "updatedBy": "admin2",
                  "updatedTime": "2023-09-24T12:00:00"
                }
              ],
              "emailAddresses": [
                {
                  "contactEmailId": 1,
                  "contactId": 123,
                  "emailAddress": "test@example.com",
                  "createdBy": "admin",
                  "createdTime": "2023-09-23T10:15:30",
                  "updatedBy": "admin2",
                  "updatedTime": "2023-09-24T12:00:00"
                }
              ],
              "identities": [
                {
                  "contactIdentityId": 1,
                  "contactId": 123,
                  "identityType": "PASS",
                  "identityTypeDescription": "Passport number",
                  "identityTypeIsActive": true,
                  "identityValue": "GB123456789",
                  "issuingAuthority": "UK Passport Office",
                  "createdBy": "admin",
                  "createdTime": "2023-09-23T10:15:30",
                  "updatedBy": "admin2",
                  "updatedTime": "2023-09-24T12:00:00"
                }
              ],
              "employments": [
                {
                  "employmentId": 123456,
                  "contactId": 654321,
                  "employer": {
                    "organisationId": 9007199254740991,
                    "organisationName": "string",
                    "organisationActive": true,
                    "flat": "string",
                    "property": "string",
                    "street": "string",
                    "area": "string",
                    "cityCode": "string",
                    "cityDescription": "string",
                    "countyCode": "string",
                    "countyDescription": "string",
                    "postcode": "string",
                    "countryCode": "string",
                    "countryDescription": "string",
                    "businessPhoneNumber": "string",
                    "businessPhoneNumberExtension": "string"
                  },
                  "isActive": true,
                  "createdBy": "admin",
                  "createdTime": "2023-09-23T10:15:30",
                  "updatedBy": "admin2",
                  "updatedTime": "2023-09-24T12:00:00"
                }
              ],
              "domesticStatusCode": "S",
              "domesticStatusDescription": "Single",
              "genderCode": "M",
              "genderDescription": "Male",
              "createdBy": "JD000001",
              "createdTime": "2024-01-01T00:00:00Z"
            }
            """.trimIndent(),
        )

        val response = personalRelationshipsGateway.getContactByContactId(contactId)
        response.errors.shouldBeEmpty()
        response.data.shouldNotBeNull()
        response.data!!
          .addresses
          .first()
          .contactId
          .shouldBe(123456)
        response.data!!.genderCode.shouldBe("M")
      }
    }

    describe("GET /prisoner/{prisonerNumber}/number-of-children") {

      it("authenticates using HMPPS Auth with credentials") {

        personalRelationshipsGateway.getNumberOfChildren(prisonerId)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("PERSONAL-RELATIONSHIPS")
      }

      it("Gets a number of children by id successfully") {
        personalRelationshipsApiMockServer.stubForGet(
          getChildrenPath,
          body =
            """
            {
              "id": 1,
              "numberOfChildren": "2",
              "active": true,
              "createdTime": "2025-04-14T10:11:25.052Z",
              "createdBy": "Person"
            }
            """.trimIndent(),
        )

        val response = personalRelationshipsGateway.getNumberOfChildren(prisonerId)
        response.errors.shouldBeEmpty()
        response.data.shouldNotBeNull()
        response.data!!.numberOfChildren.shouldBe("2")
      }

      it("Returns a bad request error") {
        personalRelationshipsApiMockServer.stubForGet(
          getChildrenPath,
          "",
          HttpStatus.BAD_REQUEST,
        )

        val response = personalRelationshipsGateway.getNumberOfChildren(prisonerId)
        response.data.shouldBe(null)
        response.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.PERSONAL_RELATIONSHIPS, type = UpstreamApiError.Type.BAD_REQUEST, description = null)))
      }
    }

    describe("GET /contact/search returns a response") {
      val requestContext = buildRequestContext()
      val fixturesPath = "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/personalRelationships/fixtures/contact-search-response.json"
      it("returns a search response") {

        personalRelationshipsApiMockServer.stubForGet(
          "/contacts/search?page=0&size=10&firstName=John&searchType=EXACT",
          body =
            File(
              fixturesPath,
            ).readText(),
        )

        val contactResponse = personalRelationshipsGateway.contactSearch(ContactSearchRequest(firstName = "John"), 1, 10, buildRequestContext())
        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("PERSONAL-RELATIONSHIPS", requestContext)
        contactResponse.data
          ?.contacts
          ?.size
          .shouldBe(1)
        contactResponse.data
          ?.contacts[0]
          ?.firstName
          .shouldBe("John")
        contactResponse.data
          ?.pageMetadata
          ?.size
          .shouldBe(10)
        contactResponse.data
          ?.pageMetadata
          ?.number
          .shouldBe(0)
        contactResponse.data
          ?.pageMetadata
          ?.totalElements
          .shouldBe(1)
        contactResponse.data
          ?.pageMetadata
          ?.totalPages
          .shouldBe(1)
      }

      it("returns a 404 response") {
        personalRelationshipsApiMockServer.stubForGet(
          "/contacts/search?page=0&size=10&firstName=John&searchType=EXACT",
          body = "",
          status = HttpStatus.NOT_FOUND,
        )
        val contactResponse = personalRelationshipsGateway.contactSearch(ContactSearchRequest(firstName = "John"), 1, 10, buildRequestContext())
        contactResponse.errors.size.shouldBe(1)
        contactResponse.errors[0].shouldBe(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.PERSONAL_RELATIONSHIPS))
      }

      it("returns a 400 response") {
        personalRelationshipsApiMockServer.stubForGet(
          "/contacts/search?page=0&size=10&searchType=EXACT",
          body = "",
          status = HttpStatus.BAD_REQUEST,
        )
        val contactResponse = personalRelationshipsGateway.contactSearch(ContactSearchRequest(), 1, 10, buildRequestContext())
        contactResponse.errors.size.shouldBe(1)
        contactResponse.errors[0].shouldBe(UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.PERSONAL_RELATIONSHIPS))
      }
    }

    it("gets a list of prisoner contact ids with page data") {
      val path = "/contact/$contactId/linked-prisoners?page=0&size=10"
      personalRelationshipsApiMockServer.stubForGet(
        path,
        body =
          """
          {
            "content": [
              {
                "prisonerNumber": "A1234BC",
                "lastName": "Doe",
                "firstName": "John",
                "middleNames": "William",
                "prisonerContactId": 123456,
                "relationshipTypeCode": "S",
                "relationshipTypeDescription": "Official",
                "relationshipToPrisonerCode": "FRI",
                "relationshipToPrisonerDescription": "Friend",
                "isRelationshipActive": true
              }
            ],
            "page": {
              "size": 10,
              "totalElements": 1,
              "totalPages": 1,
              "number": 0
            }
          }
          """.trimIndent(),
      )

      val response = personalRelationshipsGateway.getLinkedPrisoner(contactId, 1, 10)
      response.errors.shouldBeEmpty()
      response.data.shouldNotBeNull()
      response.data.prisoners
        .first()
        .prisonerNumber
        .shouldBe("A1234BC")
    }
  })
