package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.web.PagedModel
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedPrisonerContacts

internal class PRPaginatedPrisonerContactsTest {
  @DisplayName("Handle converting to PaginatedPrisonerContacts")
  @Nested
  inner class TestPaginatedPrisonerContacts {
    @Test
    fun `Handle empty data`() {
      val prPaginatedPrisonerContacts =
        PRPaginatedPrisonerContacts(
          contacts = emptyList(),
          pageMetadata =
            PagedModel.PageMetadata(
              10,
              0,
              0,
              0,
            ),
        )

      val paginatedPrisonerContacts =
        PaginatedPrisonerContacts(
          content = emptyList(),
          count = 0,
          page = 1,
          totalCount = 0,
          totalPages = 0,
          isLastPage = true,
          perPage = 10,
        )

      prPaginatedPrisonerContacts.toPaginatedPrisonerContacts().shouldBe(paginatedPrisonerContacts)
    }

    @Test
    fun `Handle data`() {
      val visitorContactId = 3L
      val prPaginatedPrisonerContacts =
        PRPaginatedPrisonerContacts(
          contacts =
            listOf(
              PRPrisonerContact(
                prisonerContactId = 123456,
                contactId = visitorContactId,
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
              ),
            ),
          pageMetadata =
            PagedModel.PageMetadata(
              10,
              0,
              1,
              1,
            ),
        )

      val paginatedPrisonerContacts =
        PaginatedPrisonerContacts(
          content = prPaginatedPrisonerContacts.contacts.map { it.toPrisonerContact() },
          count = 1,
          page = 1,
          totalCount = 1,
          totalPages = 1,
          isLastPage = true,
          perPage = 10,
        )

      prPaginatedPrisonerContacts.toPaginatedPrisonerContacts().shouldBe(paginatedPrisonerContacts)
    }
  }
}
