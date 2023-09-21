package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

data class RiskToSelf(
  val suicide: Risk = Risk(),
  val selfHarm: Risk = Risk(),
  val custody: Risk = Risk(),
  val hostelSetting: Risk = Risk(),
  val vulnerability: Risk = Risk(),
)
