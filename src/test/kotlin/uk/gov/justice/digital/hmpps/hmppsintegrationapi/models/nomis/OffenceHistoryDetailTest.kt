package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class OffenceHistoryDetailTest : DescribeSpec(
  {
    describe("#toOffence") {
      it("maps one-to-one attributes to integration API attributes") {
        val offenceHistoryDetail = OffenceHistoryDetail(
          courtDate = LocalDate.parse("1995-06-06"),
          offenceDate = LocalDate.parse("1995-04-06"),
          offenceCode = "RR84555",
          offenceDescription = "A test offence description for model testing",
          offenceRangeDate = LocalDate.parse("1995-05-06"),
          statuteCode = "RR84",
        )

        val integrationApiOffence = offenceHistoryDetail.toOffence()

        integrationApiOffence.cjsCode.shouldBe(offenceHistoryDetail.offenceCode)
        integrationApiOffence.courtDates.shouldBe(listOf(offenceHistoryDetail.courtDate))
        integrationApiOffence.description.shouldBe(offenceHistoryDetail.offenceDescription)
        integrationApiOffence.endDate.shouldBe(offenceHistoryDetail.offenceRangeDate)
        integrationApiOffence.startDate.shouldBe(offenceHistoryDetail.offenceDate)
        integrationApiOffence.statuteCode.shouldBe(offenceHistoryDetail.statuteCode)
      }

      it("deals with NULL values") {
        val offenceHistoryDetail = OffenceHistoryDetail(
          offenceCode = "RR84555",
          offenceDescription = "A test offence description for model testing",
          statuteCode = "RR84",
        )

        val integrationApiOffence = offenceHistoryDetail.toOffence()

        integrationApiOffence.cjsCode.shouldBe(offenceHistoryDetail.offenceCode)
        integrationApiOffence.courtDates.shouldBeEmpty()
        integrationApiOffence.description.shouldBe(offenceHistoryDetail.offenceDescription)
        integrationApiOffence.endDate.shouldBeNull()
        integrationApiOffence.startDate.shouldBeNull()
        integrationApiOffence.statuteCode.shouldBe(offenceHistoryDetail.statuteCode)
      }
    }
  },
)
