package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.EmailAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRDetailedContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PhoneNumber

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [(GetContactService::class)],
)
internal class GetContactServiceTest(
  private val getVisitorService: GetContactService,
  @MockitoBean val personalRelationshipsGateway: PersonalRelationshipsGateway,
) : DescribeSpec({

    val contactId = "123456"
    val contactResponse =
      PRDetailedContact(
        id = 123456L,
        title = "MR",
        titleDescription = "Mister",
        lastName = "Smith",
        firstName = "John",
        middleNames = "David",
        dateOfBirth = "1980-01-15",
        isStaff = false,
        deceasedDate = null,
        languageCode = "EN",
        languageDescription = "English",
        interpreterRequired = false,
        addresses =
          listOf(
            Address(
              contactAddressId = 456L,
              contactId = 123L,
              addressType = "HOME",
              addressTypeDescription = "Home Address",
              primaryAddress = true,
              flat = "10",
              property = "Apartment Building",
              street = "Main Street",
              area = "Downtown",
              cityCode = "LDN",
              cityDescription = "London",
              countyCode = "GL",
              countyDescription = "Greater London",
              postcode = "SW1A 1AA",
              countryCode = "GB",
              countryDescription = "United Kingdom",
              verified = true,
              verifiedBy = "System",
              verifiedTime = "2023-10-27T10:00:00Z",
              mailFlag = true,
              startDate = "2020-01-01",
              endDate = null,
              noFixedAddress = false,
              comments = null,
              phoneNumbers =
                listOf(
                  PhoneNumber(
                    contactPhoneId = 789L,
                    contactId = 123L,
                    phoneType = "HOME",
                    phoneTypeDescription = "Home Phone",
                    phoneNumber = "020 1234 5678",
                    extNumber = null,
                  ),
                ),
              createdBy = "System",
              createdTime = "2023-10-27T09:00:00Z",
              updatedBy = null,
              updatedTime = null,
            ),
            Address(
              contactAddressId = 457L,
              contactId = 123L,
              addressType = "WORK",
              addressTypeDescription = "Work Address",
              primaryAddress = false,
              flat = null,
              property = "Office Building",
              street = "Business Road",
              area = "Business Park",
              cityCode = "MCR",
              cityDescription = "Manchester",
              countyCode = "GM",
              countyDescription = "Greater Manchester",
              postcode = "M1 1AA",
              countryCode = "GB",
              countryDescription = "United Kingdom",
              verified = true,
              verifiedBy = "System",
              verifiedTime = "2024-01-10T11:00:00Z",
              mailFlag = true,
              startDate = "2024-01-01",
              endDate = null,
              noFixedAddress = false,
              comments = null,
              phoneNumbers =
                listOf(
                  PhoneNumber(
                    contactPhoneId = 790L,
                    contactId = 123L,
                    phoneType = "WORK",
                    phoneTypeDescription = "Work Phone",
                    phoneNumber = "0161 1234 567",
                    extNumber = "123",
                  ),
                ),
              createdBy = "System",
              createdTime = "2024-01-10T10:00:00Z",
              updatedBy = null,
              updatedTime = null,
            ),
          ),
        phoneNumbers =
          listOf(
            PhoneNumber(
              contactPhoneId = 901L,
              contactId = 123L,
              phoneType = "MOBILE",
              phoneTypeDescription = "Mobile Phone",
              phoneNumber = "07700 123456",
              extNumber = null,
            ),
          ),
        emailAddresses =
          listOf(
            EmailAddress(
              contactEmailId = 1011L,
              contactId = 123L,
              emailAddress = "john.smith@example.com",
            ),
            EmailAddress(
              contactEmailId = 1012L,
              contactId = 123L,
              emailAddress = "john.smith.work@example.com",
            ),
          ),
        gender = "M",
        genderDescription = "Male",
      )

    beforeEach {
      Mockito.reset(personalRelationshipsGateway)
    }

    it("will return 200 and a contact for the contactId provided") {
      val contactIdLongified = contactId.toLong()
      whenever(personalRelationshipsGateway.getContactByContactId(contactIdLongified)).thenReturn(Response(data = contactResponse))
      val response = getVisitorService.execute(contactId = contactId)
      response.data.shouldBe(contactResponse.toDetailedContact())
      response.errors.shouldBeEmpty()
    }

    it("will return 404 when no data response for contactId") {
      whenever(personalRelationshipsGateway.getContactByContactId(contactId.toLong())).thenReturn(Response(data = null))
      val response = getVisitorService.execute(contactId = contactId)
      response.errors.shouldContain(
        UpstreamApiError(
          UpstreamApi.PERSONAL_RELATIONSHIPS,
          type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
        ),
      )
    }

    it("will return 400 when invalid contactId") {
      val response = getVisitorService.execute(contactId = "THIS_ISNT_A_LONG!")
      response.errors.shouldContain(UpstreamApiError(UpstreamApi.PERSONAL_RELATIONSHIPS, UpstreamApiError.Type.BAD_REQUEST))
    }
  })
