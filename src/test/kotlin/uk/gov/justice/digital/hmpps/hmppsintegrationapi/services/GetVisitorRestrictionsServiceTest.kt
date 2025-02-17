package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.ContactGlobalRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.LinkedPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PrisonerContactRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PrisonerContactRestrictions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.Relationship
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetVisitorRestrictionsService::class],
)
class GetVisitorRestrictionsServiceTest(
  @MockitoBean val personalRelationshipsGateway: PersonalRelationshipsGateway,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val getVisitorRestrictionsService: GetVisitorRestrictionsService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val prisonId = "ABC"
      val contactId = 123456L
      val filters = ConsumerFilters(null)
      val posPrisoner = POSPrisoner(firstName = "Test", lastName = "Person", prisonId = prisonId)
      val listOfRelationships = listOf(Relationship(prisonerContactId = contactId, relationshipType = "FAM", relationshipTypeDescription = "Family", relationshipToPrisoner = "SON", relationshipToPrisonerDescription = "Son"))
      val listOfLinkedPrisoners = listOf(LinkedPrisoner(prisonerNumber = "1234567", relationships = listOfRelationships, firstName = "Test", lastName = "Person", middleNames = null))
      val prisonerContactRestrictionsResponse =
        listOf(
          PrisonerContactRestriction(
            prisonerContactRestrictionId = 1L,
            prisonerContactId = contactId,
            contactId = contactId,
            prisonerNumber = "A1234BC",
            restrictionType = "Restriction 1",
            restrictionTypeDescription = "Description for Restriction 1",
            startDate = "2024-01-01",
            expiryDate = "2025-01-01",
            comments = "Some comments",
            enteredByUsername = "user123",
            enteredByDisplayName = "User Name",
            createdBy = "admin",
            createdTime = "2024-01-01T12:00:00Z",
            updatedBy = "admin",
            updatedTime = "2024-01-02T12:00:00Z",
          ),
          PrisonerContactRestriction(
            prisonerContactRestrictionId = 2L,
            prisonerContactId = contactId,
            contactId = contactId,
            prisonerNumber = "A1234BC",
            restrictionType = "Restriction 2",
            restrictionTypeDescription = "Description for Restriction 2",
            startDate = "2024-02-01",
            expiryDate = "2025-02-01",
            comments = "Some other comments",
            enteredByUsername = "user123",
            enteredByDisplayName = "User Name",
            createdBy = "admin",
            createdTime = "2024-02-01T12:00:00Z",
            updatedBy = "admin",
            updatedTime = "2024-02-02T12:00:00Z",
          ),
        )

      val contactGlobalRestrictionsResponse =
        listOf(
          ContactGlobalRestriction(
            contactRestrictionId = 1L,
            contactId = contactId,
            restrictionType = "Restriction 1",
            restrictionTypeDescription = "Description for Restriction 1",
            startDate = "2024-01-01",
            expiryDate = "2025-01-01",
            comments = "Some comments",
            enteredByUsername = "user123",
            enteredByDisplayName = "User Name",
            createdBy = "admin",
            createdTime = "2024-01-01T12:00:00Z",
            updatedBy = "admin",
            updatedTime = "2024-01-02T12:00:00Z",
          ),
          ContactGlobalRestriction(
            contactRestrictionId = 2L,
            contactId = contactId,
            restrictionType = "Restriction 2",
            restrictionTypeDescription = "Description for Restriction 2",
            startDate = "2024-02-01",
            expiryDate = "2025-02-01",
            comments = "Some other comments",
            enteredByUsername = "user123",
            enteredByDisplayName = "User Name",
            createdBy = "admin",
            createdTime = "2024-02-01T12:00:00Z",
            updatedBy = "admin",
            updatedTime = "2024-02-02T12:00:00Z",
          ),
        )
      val prisonerContactRestrictions = PrisonerContactRestrictions(prisonerContactRestrictionsResponse, contactGlobalRestrictionsResponse)
      beforeEach {
        Mockito.reset(personalRelationshipsGateway)
        Mockito.reset(getPersonService)

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonId, filters)).thenReturn(
          Response(data = null),
        )

        whenever(getPersonService.getPrisoner(hmppsId, filters)).thenReturn(Response(data = posPrisoner.toPersonInPrison(), errors = emptyList()))
      }

      it("gets a person using a Hmpps ID") {
        whenever(personalRelationshipsGateway.getLinkedPrisoner(contactId)).thenReturn(Response(data = listOfLinkedPrisoners, errors = emptyList()))
        whenever(personalRelationshipsGateway.getPrisonerContactRestrictions(contactId)).thenReturn(Response(data = prisonerContactRestrictions, errors = emptyList()))

        getVisitorRestrictionsService.execute(hmppsId, contactId, filters)
        verify(getPersonService, VerificationModeFactory.times(1)).getPrisoner(hmppsId = hmppsId, filters = filters)
      }

      it("returns an error from person service when prisoner is not found") {
        val errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH, description = "not found."))
        whenever(getPersonService.getPrisoner(hmppsId, filters)).thenReturn(Response(data = null, errors = errors))

        val result = getVisitorRestrictionsService.execute(hmppsId, contactId, filters)
        result.data.shouldBe(emptyList())
        result.errors.shouldBe(errors)
      }

      it("returns null when a person in an unapproved prison") {
        val consumerFilters = ConsumerFilters(prisons = listOf("XYZ"))
        val wrongPrisonId = "ABC"

        whenever(getPersonService.getPrisoner(hmppsId, consumerFilters)).thenReturn(Response(data = posPrisoner.toPersonInPrison(), errors = emptyList()))

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<PrisonerContactRestrictions>>(wrongPrisonId, consumerFilters)).thenReturn(
          Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"))),
        )

        val response = getVisitorRestrictionsService.execute(hmppsId, contactId, consumerFilters)

        response.data.shouldBe(null)
        response.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
      }

      it("returns an error when linked prisoners are not found") {
        val errors = listOf(UpstreamApiError(UpstreamApi.PERSONAL_RELATIONSHIPS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"))

        whenever(personalRelationshipsGateway.getLinkedPrisoner(contactId)).thenReturn(Response(data = emptyList(), errors = errors))

        val response = getVisitorRestrictionsService.execute(hmppsId, contactId, filters)

        response.data.shouldBe(emptyList())
        response.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PERSONAL_RELATIONSHIPS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
      }

      it("returns visitor restrictions") {
        whenever(personalRelationshipsGateway.getLinkedPrisoner(contactId)).thenReturn(Response(data = listOfLinkedPrisoners, errors = emptyList()))
        whenever(personalRelationshipsGateway.getPrisonerContactRestrictions(contactId)).thenReturn(Response(data = prisonerContactRestrictions, errors = emptyList()))

        val response = getVisitorRestrictionsService.execute(hmppsId, contactId, filters)

        response.data.shouldBe(listOf(prisonerContactRestrictions))
      }

      it("returns multiple relationships and queries getPrisonerContactRestrictions accordingly") {
        val scopedPrisonerContactId = 123457L
        val listOfManyRelationships = listOf(Relationship(prisonerContactId = contactId, relationshipType = "FAM", relationshipTypeDescription = "Family", relationshipToPrisoner = "SON", relationshipToPrisonerDescription = "Son"), Relationship(prisonerContactId = scopedPrisonerContactId, relationshipType = "FAM", relationshipTypeDescription = "Family", relationshipToPrisoner = "BRO", relationshipToPrisonerDescription = "Brother"))
        val listOfLinkedPrisonerWithManyRelationships = listOf(LinkedPrisoner(prisonerNumber = "1234567", relationships = listOfManyRelationships, firstName = "Test", lastName = "Person", middleNames = null))

        whenever(personalRelationshipsGateway.getLinkedPrisoner(contactId)).thenReturn(Response(data = listOfLinkedPrisonerWithManyRelationships, errors = emptyList()))
        whenever(personalRelationshipsGateway.getPrisonerContactRestrictions(contactId)).thenReturn(Response(data = prisonerContactRestrictions, errors = emptyList()))
        whenever(personalRelationshipsGateway.getPrisonerContactRestrictions(scopedPrisonerContactId)).thenReturn(Response(data = prisonerContactRestrictions, errors = emptyList()))

        getVisitorRestrictionsService.execute(hmppsId, contactId, filters)

        verify(personalRelationshipsGateway, VerificationModeFactory.times(2)).getPrisonerContactRestrictions(Mockito.anyLong())
      }
    },
  )
