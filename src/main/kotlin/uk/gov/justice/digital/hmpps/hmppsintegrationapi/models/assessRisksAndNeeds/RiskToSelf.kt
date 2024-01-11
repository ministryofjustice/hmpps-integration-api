package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskToSelf as IntegrationApiRiskToSelf

data class RiskToSelf(
  val suicide: Risk = Risk(),
  val selfHarm: Risk = Risk(),
  val custody: Risk = Risk(),
  val hostelSetting: Risk = Risk(),
  val vulnerability: Risk = Risk(),
) {
  fun toRiskToSelf(): IntegrationApiRiskToSelf = IntegrationApiRiskToSelf(
    suicide = this.suicide.toRisk(),
    selfHarm = this.selfHarm.toRisk(),
    custody = this.custody.toRisk(),
    hostelSetting = this.hostelSetting.toRisk(),
    vulnerability = this.vulnerability.toRisk(),
  )
}
