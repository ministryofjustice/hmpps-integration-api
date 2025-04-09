package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Contact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContactRelationship
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRPaginatedPrisonerContacts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRPrisonerContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.Pageable
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.RestrictionSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.Sort
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonerContactsService::class],
)
internal class GetPrisonerContactsServiceTest(
  @MockitoBean val personalRelationshipsGateway: PersonalRelationshipsGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getPrisonerContactsService: GetPrisonerContactsService,
) : DescribeSpec({
    val hmppsId = "A1234AA"
    val filters = ConsumerFilters(null)
    val page = 1
    val size = 10
    val relationship =
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
      )
    val contact =
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
      )
    val personalRelationshipsContactResponseInstance =
      PRPrisonerContact(
        prisonerContactId = 123456,
        contactId = 654321,
        prisonerNumber = "A1234BC",
        lastName = "Doe",
        firstName = "John",
        middleNames = "William",
        dateOfBirth = "1980-01-01",
        relationshipTypeCode = "S",
        relationshipTypeDescription = "Friend",
        relationshipToPrisonerCode = "FRI",
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
        isApprovedVisitor = true,
        isNextOfKin = false,
        isEmergencyContact = true,
        isRelationshipActive = true,
        currentTerm = true,
        comments = "Close family friend",
        restrictionSummary = RestrictionSummary(active = emptyList(), totalActive = 0, totalExpired = 0),
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
      PRPaginatedPrisonerContacts(
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
      whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(data = NomisNumber(hmppsId)))
    }

    it("returns a list of contacts with pagination details") {
      whenever(personalRelationshipsGateway.getContacts(hmppsId, page, size)).thenReturn(Response(data = prPaginatedContactsInstance, errors = emptyList()))

      val result = getPrisonerContactsService.execute(hmppsId, page, size, filters)

      result.shouldNotBeNull()
      result.shouldBe(Response(data = prPaginatedContactsInstance.toPaginatedPrisonerContacts()))
    }

    it("failed personal relationship call") {
      val err = listOf(UpstreamApiError(UpstreamApi.PERSONAL_RELATIONSHIPS, UpstreamApiError.Type.INTERNAL_SERVER_ERROR))
      whenever(personalRelationshipsGateway.getContacts(hmppsId, page, size)).thenReturn(Response(data = null, errors = err))
      val result = getPrisonerContactsService.execute(hmppsId, page, size, filters)
      result.errors.shouldBe(err)
    }

    it("failed prison check call") {
      val err = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "NOMIS number not found"))
      whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(data = null, errors = err))
      val result = getPrisonerContactsService.execute(hmppsId, page, size, filters)
      result.errors.shouldBe(err)
    }

    it("failed to get prisoners nomis number") {
      val err = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND))
      whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(data = NomisNumber(), errors = emptyList()))
      val result = getPrisonerContactsService.execute(hmppsId, page, size, filters)
      result.errors.shouldBe(err)
    }
  })
