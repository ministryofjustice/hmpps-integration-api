package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.RiskToSelf as ArnRiskToSelf

class RiskToSelfTest : DescribeSpec(
  {
    describe("#toRiskToSelf") {
      it("maps one-to-one attributes to Integration API attributes") {
        val arnRisk = ArnRiskToSelf(
          suicide = Risk(risk = "risk"),
          selfHarm = Risk(risk = "risk"),
          custody = Risk(risk = "risk"),
          hostelSetting = Risk(risk = "risk"),
          vulnerability = Risk(risk = "risk"),
        )

        val integrationApiRisk = arnRisk.toRiskToSelf()

        integrationApiRisk.suicide.risk.shouldBe(arnRisk.suicide.risk)
        integrationApiRisk.selfHarm.risk.shouldBe(arnRisk.selfHarm.risk)
        integrationApiRisk.custody.risk.shouldBe(arnRisk.custody.risk)
        integrationApiRisk.hostelSetting.risk.shouldBe(arnRisk.hostelSetting.risk)
        integrationApiRisk.vulnerability.risk.shouldBe(arnRisk.vulnerability.risk)
      }
    }
  },
)
