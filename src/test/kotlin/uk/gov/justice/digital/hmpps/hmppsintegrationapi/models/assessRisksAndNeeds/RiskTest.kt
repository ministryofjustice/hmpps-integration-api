package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.Risk as ArnRisk

class RiskTest : DescribeSpec(
  {
    describe("#toRisk") {
      it("maps ARN Risk to Integration API Risk") {
        val arnRisk = ArnRisk(
          assessedOn = LocalDateTime.of(2021, 6, 21, 15, 55, 4),
        )

        val integrationApiRisk = arnRisk.toRisk()

        integrationApiRisk.assessedOn.shouldBe(
          arnRisk.assessedOn,
        )
      }
    }
  },
)
