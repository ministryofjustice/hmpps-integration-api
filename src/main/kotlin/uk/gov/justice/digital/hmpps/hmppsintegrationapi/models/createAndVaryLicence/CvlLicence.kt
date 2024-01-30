package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.createAndVaryLicence

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LicenceCondition

class CvlLicence(
  val conditions: CvlLicenceCondition? = null,
) {
  fun toLicenceConditions(): List<LicenceCondition> = conditions?.toLicenceConditions() ?: emptyList()
}
