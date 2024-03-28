package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskToSelf

data class ArnRiskToSelf(
  val suicide: ArnRisk = ArnRisk(),
  val selfHarm: ArnRisk = ArnRisk(),
  val custody: ArnRisk = ArnRisk(),
  val hostelSetting: ArnRisk = ArnRisk(),
  val vulnerability: ArnRisk = ArnRisk(),
) {
  fun toRiskToSelf(): RiskToSelf =
    RiskToSelf(
      suicide = this.suicide.toRisk(),
      selfHarm = this.selfHarm.toRisk(),
      custody = this.custody.toRisk(),
      hostelSetting = this.hostelSetting.toRisk(),
      vulnerability = this.vulnerability.toRisk(),
    )
}
