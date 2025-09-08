package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.ContactEventStubGenerator.generateNDeliusContactEvents

internal class StubTest :
  DescribeSpec({

    it("generates paginated data correctly for first page") {
      val paginated = generateNDeliusContactEvents(crn = "A123456", pageSize = 3, pageNumber = 1, totalRecords = 10)
      paginated.page shouldBe 1
      paginated.totalPages shouldBe 4
      paginated.size shouldBe 3
      paginated.totalResults shouldBe 10
      paginated.contactEvents.size shouldBe 3
      paginated.contactEvents[0].contactEventIdentifier shouldBe 1
    }

    it("generates paginated data correctly for second page") {
      val paginated = generateNDeliusContactEvents(crn = "A123456", pageSize = 3, pageNumber = 2, totalRecords = 10)
      paginated.page shouldBe 2
      paginated.totalPages shouldBe 4
      paginated.size shouldBe 3
      paginated.totalResults shouldBe 10
      paginated.contactEvents.size shouldBe 3
      paginated.contactEvents[0].contactEventIdentifier shouldBe 4
    }

    it("generates paginated data correctly for last page") {
      val paginated = generateNDeliusContactEvents(crn = "A123456", pageSize = 3, pageNumber = 4, totalRecords = 10)
      paginated.page shouldBe 4
      paginated.totalPages shouldBe 4
      paginated.size shouldBe 3
      paginated.totalResults shouldBe 10
      paginated.contactEvents.size shouldBe 1
      paginated.contactEvents[0].contactEventIdentifier shouldBe 10
    }

    it("page requested is out of bounds") {
      val paginated = generateNDeliusContactEvents(crn = "A123456", pageSize = 3, pageNumber = 5, totalRecords = 10)
      paginated.page shouldBe 5
      paginated.totalPages shouldBe 4
      paginated.size shouldBe 3
      paginated.totalResults shouldBe 10
      paginated.contactEvents.size shouldBe 0
    }
  })
