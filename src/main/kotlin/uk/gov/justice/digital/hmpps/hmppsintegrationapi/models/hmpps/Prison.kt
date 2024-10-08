package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class Prison(
  @Schema(description = "The prison code, which is usually short for the prison name.")
  val code: String? = null,
)
