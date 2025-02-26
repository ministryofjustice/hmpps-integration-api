package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.personalRelationships

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.PersonalRelationshipsApiMockServer

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

    val personalRelationshipsApiMockServer = PersonalRelationshipsApiMockServer()

    beforeEach {
      personalRelationshipsApiMockServer.start()
      Mockito.reset(hmppsAuthGateway)

      whenever(hmppsAuthGateway.getClientToken("PERSONAL-RELATIONSHIPS")).thenReturn(HmppsAuthMockServer.TOKEN)
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
      personalRelationshipsApiMockServer.stubPersonalRelationshipsApiResponse(
        path,
        body =
          """
          [
            {
              "prisonerNumber": "A1234BC",
              "lastName": "Doe",
              "firstName": "John",
              "middleNames": "William",
              "relationships": [
                {
                  "prisonerContactId": 123456,
                  "relationshipType": "S",
                  "relationshipTypeDescription": "Official",
                  "relationshipToPrisoner": "FRI",
                  "relationshipToPrisonerDescription": "Friend"
                }
              ]
            }
          ]
          """.trimIndent(),
      )

      val response = personalRelationshipsGateway.getLinkedPrisoner(contactId)
      response.errors.shouldBeEmpty()
      response.data.shouldNotBeNull()
      response.data[0]
        .relationships!!
        .first()
        .prisonerContactId
        .shouldBe(123456)
    }

    // "Get the restrictions that apply for a particular relationship."
    it("Get the restrictions that apply for a particular relationship") {
      val path = "/prisoner-contact/$prisonerContactId/restriction"

      personalRelationshipsApiMockServer.stubPersonalRelationshipsApiResponse(
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
        personalRelationshipsApiMockServer.stubPersonalRelationshipsApiResponse(
          path,
          body =
            """
            {
              "id": 123456,
              "title": "MR",
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
              "gender": "M",
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
        response.data!!.gender.shouldBe("M")
      }
    }
  })
