package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.ContactDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.ContactInformation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRPaginatedContacts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.Pageable
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PersonalRelationshipsContactResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.Relationship
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.Sort
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonerContactsService::class],
)
internal class GetPrisonerContactsServiceTest(
  @MockitoBean val personalRelationshipsGateway: PersonalRelationshipsGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @Autowired val objectMapper: ObjectMapper,
  private val getPrisonerContactsService: GetPrisonerContactsService,
) : DescribeSpec({
    val hmppsId = "A1234AA"
    val prisonId = "ABC"
    val contactId = 123456L
    val filters = ConsumerFilters(null)
    val page = 1
    val size = 10
    val relationship =
      Relationship(
        relationshipType = "FRIEND",
        relationshipTypeDescription = "Friend",
        relationshipToPrisoner = "FRI",
        relationshipToPrisonerDescription = "Friend of",
        approvedPrisoner = true,
        nextOfKin = false,
        emergencyContact = true,
        isRelationshipActive = true,
        currentTerm = true,
        comments = "Close family friend",
      )
    val contact =
      ContactInformation(
        contactId = "654321",
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
      )
    val contactDetails =
      ContactDetails(
        contact = contact,
        relationship = relationship,
      )
    val personalRelationshipsContactResponseInstance =
      PersonalRelationshipsContactResponse(
        prisonerContactId = 123456,
        contactId = 654321,
        prisonerNumber = "A1234BC",
        lastName = "Doe",
        firstName = "John",
        middleNames = "William",
        dateOfBirth = "1980-01-01",
        relationshipType = "S",
        relationshipTypeDescription = "Friend",
        relationshipToPrisoner = "FRI",
        relationshipToPrisonerDescription = "Friend",
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
        approvedVisitor = true,
        nextOfKin = false,
        emergencyContact = true,
        isRelationshipActive = true,
        currentTerm = true,
        comments = "Close family friend",
      )
    val sortInstance =
      Sort(
        empty = true,
        sorted = true,
        unsorted = false,
      )
    val pageableInstance =
      Pageable(
        offset = 9007199254740991,
        sort = sortInstance,
        pageSize = 1073741824,
        paged = true,
        pageNumber = 1073741824,
        unpaged = true,
      )
    val prPaginatedContactsInstance =
      PRPaginatedContacts(
        contacts = listOf(personalRelationshipsContactResponseInstance),
        pageable = pageableInstance,
        totalElements = 9007199254740991,
        totalPages = 1073741824,
        first = true,
        last = true,
        size = 1073741824,
        number = 1073741824,
        sort = sortInstance,
        numberOfElements = 1073741824,
        empty = true,
      )
    beforeEach {
      Mockito.reset(personalRelationshipsGateway)

      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonId, filters)).thenReturn(
        Response(data = null),
      )
    }

    it("returns a list of contacts with pagination details") {
      whenever(personalRelationshipsGateway.getContacts(hmppsId, page, size)).thenReturn(Response(data = prPaginatedContactsInstance, errors = emptyList()))

//      val data: PaginatedResponse<ContactDetails> = PaginatedResponse(contactDetails)

      val result = getPrisonerContactsService.execute(hmppsId, page, size)

      result.shouldNotBeNull()
    }
  })
