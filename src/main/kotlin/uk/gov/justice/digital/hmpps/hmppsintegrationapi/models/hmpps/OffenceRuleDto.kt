package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class OffenceRuleDto(
  val paragraphNumber: String? = null,
  val paragraphDescription: String? = null,
  val nomisCode: String? = null,
  val withOthersNomisCode: String? = null,
)
