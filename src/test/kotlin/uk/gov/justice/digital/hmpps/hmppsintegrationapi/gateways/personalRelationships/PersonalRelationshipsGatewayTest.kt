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
  })
