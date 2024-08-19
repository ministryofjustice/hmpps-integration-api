package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class NonDtoDate(
  @Schema(description = "Release date for non-DTO sentence (if applicable). This will be based on one of ARD, CRD, NPD or PRRD. NonDto stands for Non-Detention training order.", example = "2023-03-01")
  val date: LocalDate? = null,
  @Schema(
    description = """
      Indicates which type of non-DTO release date is the effective release date. One of 'ARD', 'CRD', 'NPD' or 'PRRD'. Possible values are:
      `ARD`,
      `CRD`,
      `NPD`,
      `PRRD`.
    """,
    example = "ARD",
    allowableValues = ["ARD", "CRD", "NPD", "PRRD"],
  )
  val releaseDateType: String? = null,
)
