package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskAssessment

data class NomisAssessment(
  val classificationCode: String? = null,
) {
  fun toRiskAssessment() = RiskAssessment(
    classificationCode = this.classificationCode,
  )
}
