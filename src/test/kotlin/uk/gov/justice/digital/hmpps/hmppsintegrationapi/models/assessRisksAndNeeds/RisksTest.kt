package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.Risks as ArnRisks

class RisksTest : DescribeSpec(
  {
    describe("#toRisks") {
      it("maps one-to-one attributes to Integration API attributes") {
        val arnRisks = ArnRisks(
          assessedOn = LocalDateTime.now(),
          riskToSelf = RiskToSelf(
            suicide = Risk(risk = "risk"),
            selfHarm = Risk(risk = "risk"),
            custody = Risk(risk = "risk"),
            hostelSetting = Risk(risk = "risk"),
            vulnerability = Risk(risk = "risk"),
          ),
        )

        val integrationApiRisks = arnRisks.toRisks()

        integrationApiRisks.assessedOn.shouldBe(arnRisks.assessedOn)
        integrationApiRisks.riskToSelf.suicide.risk.shouldBe(arnRisks.riskToSelf.suicide.risk)
        integrationApiRisks.riskToSelf.selfHarm.risk.shouldBe(arnRisks.riskToSelf.selfHarm.risk)
        integrationApiRisks.riskToSelf.custody.risk.shouldBe(arnRisks.riskToSelf.custody.risk)
        integrationApiRisks.riskToSelf.hostelSetting.risk.shouldBe(arnRisks.riskToSelf.hostelSetting.risk)
        integrationApiRisks.riskToSelf.vulnerability.risk.shouldBe(arnRisks.riskToSelf.vulnerability.risk)
      }
    }
  },
)
