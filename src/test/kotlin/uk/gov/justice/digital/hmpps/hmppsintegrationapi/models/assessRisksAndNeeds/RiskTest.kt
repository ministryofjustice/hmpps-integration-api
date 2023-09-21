package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.Risk as ArnRisk

class RiskTest : DescribeSpec(
  {
    describe("#toRisk") {
      it("maps one-to-one attributes to Integration API attributes") {
        val arnRisk = ArnRisk(
          risk = "risk",
          previous = "previous",
          previousConcernsText = "previousConcernsText",
          current = "current",
          currentConcernsText = "currentConcernsText",
        )

        val integrationApiRisk = arnRisk.toRisk()

        integrationApiRisk.risk.shouldBe(arnRisk.risk)
        integrationApiRisk.previous.shouldBe(arnRisk.previous)
        integrationApiRisk.previousConcernsText.shouldBe(arnRisk.previousConcernsText)
        integrationApiRisk.current.shouldBe(arnRisk.current)
        integrationApiRisk.currentConcernsText.shouldBe(arnRisk.currentConcernsText)
      }
    }
  },
)
