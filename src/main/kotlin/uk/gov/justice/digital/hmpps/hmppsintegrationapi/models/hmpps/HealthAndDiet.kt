package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class HealthAndDiet(
  @Schema(description = "Smoker status", examples = ["Y", "N", "V"])
  val smoking: String? = null,
)
