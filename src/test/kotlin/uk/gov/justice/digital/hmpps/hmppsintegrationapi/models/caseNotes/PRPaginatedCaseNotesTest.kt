package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.caseNotes

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedCaseNotes
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.OCNCaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.OCNPagination
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiCaseNote

internal class PRPaginatedCaseNotesTest {
  @DisplayName("Handle converting to PaginatedCaseNotes")
  @Nested
  inner class TestPaginatedCaseNotes {
    @Test
    fun `Handle empty data`() {
      val prPaginatedCaseNotes =
        OCNCaseNote(
          content = listOf(),
          page = OCNPagination(page = 1, size = 10, totalElements = 0),
        )

      val paginatedCaseNotes =
        PaginatedCaseNotes(
          content = prPaginatedCaseNotes.content.map { it.toCaseNote() },
          count = 0,
          page = 1,
          totalCount = 0,
          totalPages = 0,
          isLastPage = true,
          perPage = 10,
        )

      prPaginatedCaseNotes.toPaginatedCaseNotes().shouldBe(paginatedCaseNotes)
    }

    @Test
    fun `Handle data`() {
      val prPaginatedCaseNotes =
        OCNCaseNote(
          content = listOf(PrisonApiCaseNote(caseNoteId = "abcd1234")),
          page = OCNPagination(page = 1, size = 10, totalElements = 1),
        )

      val paginatedCaseNotes =
        PaginatedCaseNotes(
          content = prPaginatedCaseNotes.content.map { it.toCaseNote() },
          count = 1,
          page = 1,
          totalCount = 1,
          totalPages = 1,
          isLastPage = true,
          perPage = 10,
        )

      prPaginatedCaseNotes.toPaginatedCaseNotes().shouldBe(paginatedCaseNotes)
    }

    @Test
    fun `Handle multiple pages - Page 1 of 2`() {
      val prPaginatedCaseNotes =
        OCNCaseNote(
          content = listOf(PrisonApiCaseNote(caseNoteId = "abcd1234")),
          page = OCNPagination(page = 1, size = 10, totalElements = 20),
        )

      val paginatedCaseNotes =
        PaginatedCaseNotes(
          content = prPaginatedCaseNotes.content.map { it.toCaseNote() },
          count = 1,
          page = 1,
          totalCount = 20,
          totalPages = 2,
          isLastPage = false,
          perPage = 10,
        )

      prPaginatedCaseNotes.toPaginatedCaseNotes().shouldBe(paginatedCaseNotes)
    }

    @Test
    fun `Handle multiple pages - Page 2 of 2`() {
      val prPaginatedCaseNotes =
        OCNCaseNote(
          content = listOf(PrisonApiCaseNote(caseNoteId = "abcd1234")),
          page = OCNPagination(page = 2, size = 10, totalElements = 20),
        )

      val paginatedCaseNotes =
        PaginatedCaseNotes(
          content = prPaginatedCaseNotes.content.map { it.toCaseNote() },
          count = 1,
          page = 2,
          totalCount = 20,
          totalPages = 2,
          isLastPage = true,
          perPage = 10,
        )

      prPaginatedCaseNotes.toPaginatedCaseNotes().shouldBe(paginatedCaseNotes)
    }
  }
}
