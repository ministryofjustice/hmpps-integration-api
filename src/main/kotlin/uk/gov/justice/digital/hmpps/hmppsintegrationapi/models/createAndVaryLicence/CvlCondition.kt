package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.createAndVaryLicence

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LicenceCondition

class CvlCondition(
  val text: String? = null,
) {
  fun toLicenceCondition(): LicenceCondition = LicenceCondition(
    condition = this.text,
  )
}
