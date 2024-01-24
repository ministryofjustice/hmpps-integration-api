package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.createAndVaryLicence

import com.fasterxml.jackson.annotation.JsonProperty

class CvlLicenceCondition(
  @JsonProperty("AP")
  val AP: CvlAPCondition? = null,
)
