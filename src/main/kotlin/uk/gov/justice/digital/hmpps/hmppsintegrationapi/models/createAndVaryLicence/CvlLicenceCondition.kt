package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.createAndVaryLicence

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LicenceCondition

class CvlLicenceCondition(
  @JsonProperty("AP")
  val AP: CvlAPCondition? = null,
  @JsonProperty("PSS")
  val PSS: CvlPSSCondition? = null,
) {
  fun toLicenceConditions(): List<LicenceCondition> {
    val conditions = mutableListOf<LicenceCondition>()

    AP?.bespoke?.let { it -> conditions.addAll(it.map { it.toLicenceCondition("Bespoke") }) }
    AP?.standard?.let { it -> conditions.addAll(it.map { it.toLicenceCondition("Standard") }) }
    AP?.additional?.let { it -> conditions.addAll(it.map { it.toLicenceCondition() }) }
    PSS?.additional?.let { it -> conditions.addAll(it.map { it.toLicenceCondition() }) }
    PSS?.standard?.let { it -> conditions.addAll(it.map { it.toLicenceCondition("Standard") }) }
    return conditions
  }
}
