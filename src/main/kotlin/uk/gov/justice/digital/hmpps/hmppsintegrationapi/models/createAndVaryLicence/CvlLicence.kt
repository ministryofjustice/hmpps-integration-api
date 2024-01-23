package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.createAndVaryLicence

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LicenceCondition

class CvlLicence(
  val conditions: CvlLicenceCondition? = null,
) {
  fun toLicenceConditions(): List<LicenceCondition> {
    val result = mutableListOf<LicenceCondition>()
    conditions?.AP?.standard?.forEach { result.add(it.toLicenceCondition()) }
    return result
  }
}
