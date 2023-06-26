package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class OffenceHistoryDetailTest : DescribeSpec(
  {
    describe("#toOffence") {
      it("maps one-to-one attributes to integration API attributes") {
        val offenceHistoryDetail = OffenceHistoryDetail(
          offenceDate = LocalDate.parse("1995-04-06"),
          offenceCode = "RR84555",
          offenceDescription = "A test offence description for model testing",
        )

        val integrationApiOffence = offenceHistoryDetail.toOffence()

        integrationApiOffence.date.shouldBe(offenceHistoryDetail.offenceDate)
        integrationApiOffence.code.shouldBe(offenceHistoryDetail.offenceCode)
        integrationApiOffence.description.shouldBe(offenceHistoryDetail.offenceDescription)
      }
    }
  },
)
