package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class NeedSection(
  val section: String? = null,
  val linkedToReOffending: String? = null,
  val linkedToHarm: String? = null,
  val questionAnswers: HashMap<String, String>? = null,
  val score: Int? = null,
  val parentalResponsibilities: String? = null,
  val impulsivity: String? = null,
  val temperControl: String? = null,
)
