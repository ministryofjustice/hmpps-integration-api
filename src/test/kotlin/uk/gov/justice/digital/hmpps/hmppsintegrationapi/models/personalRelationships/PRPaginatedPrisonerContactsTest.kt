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
    fun `Handle converting to PaginatedPrisonerContacts`() {
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
  }
}
