package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

internal class PaginateTest : DescribeSpec({
  data class Result(val property: String = "value")

  it("returns paginated response for empty results") {
    val paginatedResponse = emptyList<Result>().paginateWith()

    paginatedResponse.data.shouldBeEmpty()
    paginatedResponse.pagination.isLastPage.shouldBeTrue()
    paginatedResponse.pagination.count.shouldBe(0)
    paginatedResponse.pagination.page.shouldBe(1)
    paginatedResponse.pagination.perPage.shouldBe(10)
    paginatedResponse.pagination.count.shouldBe(0)
    paginatedResponse.pagination.totalCount.shouldBe(0)
    paginatedResponse.pagination.totalPages.shouldBe(0)
  }

  it("returns paginated response for one page of results") {
    val results = List(3) { Result() }

    val paginatedResponse = results.paginateWith()

    paginatedResponse.data.shouldHaveSize(3)
    paginatedResponse.pagination.isLastPage.shouldBeTrue()
    paginatedResponse.pagination.count.shouldBe(3)
    paginatedResponse.pagination.page.shouldBe(1)
    paginatedResponse.pagination.perPage.shouldBe(10)
    paginatedResponse.pagination.count.shouldBe(3)
    paginatedResponse.pagination.totalCount.shouldBe(3)
    paginatedResponse.pagination.totalPages.shouldBe(1)
  }

  it("returns paginated response for multiple pages of results") {
    val results = List(20) { Result() }

    val firstPaginatedResponse = results.paginateWith()

    firstPaginatedResponse.data.shouldHaveSize(10)
    firstPaginatedResponse.pagination.isLastPage.shouldBeFalse()
    firstPaginatedResponse.pagination.count.shouldBe(10)
    firstPaginatedResponse.pagination.page.shouldBe(1)
    firstPaginatedResponse.pagination.perPage.shouldBe(10)
    firstPaginatedResponse.pagination.count.shouldBe(10)
    firstPaginatedResponse.pagination.totalCount.shouldBe(20)
    firstPaginatedResponse.pagination.totalPages.shouldBe(2)

    val secondPaginatedResponse = results.paginateWith(page = 2)

    secondPaginatedResponse.data.shouldHaveSize(10)
    secondPaginatedResponse.pagination.isLastPage.shouldBeTrue()
    secondPaginatedResponse.pagination.count.shouldBe(10)
    secondPaginatedResponse.pagination.page.shouldBe(2)
    secondPaginatedResponse.pagination.perPage.shouldBe(10)
    secondPaginatedResponse.pagination.count.shouldBe(10)
    secondPaginatedResponse.pagination.totalCount.shouldBe(20)
    secondPaginatedResponse.pagination.totalPages.shouldBe(2)
  }

  it("returns paginated response for out of bounds page") {
    val results = List(1) { Result() }
    val pageNumberThatDoesntExist = 99

    val paginatedResponse = results.paginateWith(page = pageNumberThatDoesntExist)

    paginatedResponse.data.shouldHaveSize(0)
    paginatedResponse.pagination.isLastPage.shouldBeTrue()
    paginatedResponse.pagination.count.shouldBe(0)
    paginatedResponse.pagination.page.shouldBe(pageNumberThatDoesntExist)
    paginatedResponse.pagination.perPage.shouldBe(10)
    paginatedResponse.pagination.count.shouldBe(0)
    paginatedResponse.pagination.totalCount.shouldBe(1)
    paginatedResponse.pagination.totalPages.shouldBe(1)
  }

  it("returns paginated response for the number of results specified per page") {
    val results = List(23) { Result() }

    val firstPaginatedResponse = results.paginateWith(perPage = 5)

    firstPaginatedResponse.pagination.count.shouldBe(5)
    firstPaginatedResponse.pagination.perPage.shouldBe(5)
    firstPaginatedResponse.pagination.totalPages.shouldBe(5)

    val lastPaginatedResponse = results.paginateWith(page = 5, perPage = 5)

    lastPaginatedResponse.pagination.count.shouldBe(3)
    lastPaginatedResponse.pagination.perPage.shouldBe(5)
    lastPaginatedResponse.pagination.totalPages.shouldBe(5)
  }
},)
