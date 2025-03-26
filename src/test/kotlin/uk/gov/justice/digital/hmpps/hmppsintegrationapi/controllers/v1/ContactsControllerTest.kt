package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.EmailAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRDetailedContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PhoneNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetContactService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [ContactsController::class])
@ActiveProfiles("test")
internal class ContactsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getContactService: GetContactService,
  @MockitoBean val getCaseAccess: GetCaseAccess,
) : DescribeSpec({
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)
    val path = "/v1/contacts/"
    val defaultContactId = "12345"
    val contactResponse =
      PRDetailedContact(
        id = 123456L,
        titleCode = "MR",
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
        genderCode = "M",
        genderDescription = "Male",
      )

    describe("GET Contacts") {
      beforeTest {
        Mockito.reset(auditService)
        Mockito.reset(getContactService)

        whenever(getContactService.execute(defaultContactId)).thenReturn(
          Response(
            data = contactResponse.toDetailedContact(),
          ),
        )
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised("$path$defaultContactId")

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("returns a 404 status code when no data found") {
        whenever(getContactService.execute(defaultContactId)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PERSONAL_RELATIONSHIPS, UpstreamApiError.Type.ENTITY_NOT_FOUND))))

        val result = mockMvc.performAuthorised("$path$defaultContactId")
        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("returns a 400 status code when invalid contactId supplied") {
        whenever(getContactService.execute(defaultContactId)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PERSONAL_RELATIONSHIPS, UpstreamApiError.Type.BAD_REQUEST))))

        val result = mockMvc.performAuthorised("$path$defaultContactId")
        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }
    }
  })
