package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class RiskToSelfTest : DescribeSpec(
  {
    describe("#toRiskToSelf") {
      it("maps one-to-one attributes to Integration API attributes") {
        val arnRisk =
          ArnRiskToSelf(
            suicide = ArnRisk(risk = "risk"),
            selfHarm = ArnRisk(risk = "risk"),
            custody = ArnRisk(risk = "risk"),
            hostelSetting = ArnRisk(risk = "risk"),
            vulnerability = ArnRisk(risk = "risk"),
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
