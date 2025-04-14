package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.contacts

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PersonalRelationshipsGateway::class],
)
class GetContactsGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val personalRelationshipsGateway: PersonalRelationshipsGateway,
) : DescribeSpec(
    {
      val prisonerId = "A1234BC"
      val getContactsPath = "/prisoner/$prisonerId/contact"
      val page = 1
      val size = 10
      val pathWithQueryParams = "$getContactsPath?page=${page - 1}&size=$size"
      val personalRelationshipsApiMockServer = ApiMockServer.create(UpstreamApi.PERSONAL_RELATIONSHIPS)

      beforeEach {
        personalRelationshipsApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)

        whenever(hmppsAuthGateway.getClientToken("PERSONAL-RELATIONSHIPS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        personalRelationshipsApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials for linked prisoners api") {
        personalRelationshipsGateway.getContacts(prisonerId, page, size)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("PERSONAL-RELATIONSHIPS")
      }

      it("returns a 200 when contacts are found") {
        val exampleData =
          """
          {
            "content": [
              {
                "prisonerContactId": 123456,
                "contactId": 654321,
                "prisonerNumber": "A1234BC",
                "lastName": "Doe",
                "firstName": "John",
                "middleNames": "William",
                "dateOfBirth": "1980-01-01",
                "relationshipTypeCode": "S",
                "relationshipTypeDescription": "Friend",
                "relationshipToPrisonerCode": "FRI",
                "relationshipToPrisonerDescription": "Friend",
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
                "extNumber": "123",
                "approvedVisitor": true,
                "nextOfKin": false,
                "emergencyContact": true,
                "isRelationshipActive": true,
                "currentTerm": true,
                "comments": "Close family friend"
              }
            ],
            "page": {
              "size": 10,
              "totalElements": 1,
              "totalPages": 1,
              "number": 0
            }
          }
          """.trimIndent()

        personalRelationshipsApiMockServer.stubForGet(pathWithQueryParams, body = exampleData, HttpStatus.OK)

        val response = personalRelationshipsGateway.getContacts(prisonerId, page, size)
        response.data.shouldNotBeNull()
        response.data!!.contacts.shouldHaveSize(1)
        response.data!!
          .contacts
          .first()
          .firstName
          .shouldBe("John")
      }

      it("returns a 404 when visit is not found") {
        personalRelationshipsApiMockServer.stubForGet(pathWithQueryParams, body = "", HttpStatus.NOT_FOUND)

        val response = personalRelationshipsGateway.getContacts(prisonerId, page, size)
        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.PERSONAL_RELATIONSHIPS)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
