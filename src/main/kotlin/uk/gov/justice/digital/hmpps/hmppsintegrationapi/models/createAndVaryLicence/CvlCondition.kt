package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.createAndVaryLicence

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LicenceCondition

class CvlCondition(
  var type: String? = null,
  var code: String? = null,
  var category: String? = null,
  val text: String? = null,
) {
  fun toLicenceCondition(parentType: String? = null): LicenceCondition = LicenceCondition(
    condition = this.text,
    category = this.category,
    code = this.code,
    type = parentType ?: this.type,
  )
}
