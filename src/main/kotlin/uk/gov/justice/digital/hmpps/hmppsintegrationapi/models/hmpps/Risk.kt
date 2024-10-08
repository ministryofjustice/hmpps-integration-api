package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class Risk(
  @Schema(
    description = """
      Presence of risk. Possible values are:
      `YES`,
      `NO`,
      `DK`,
      `NA`
    """,
    example = "YES",
    allowableValues = ["YES", "NO", "DK", "NA"],
  )
  val risk: String? = null,
  @Schema(
    description = """
      Previous concerns. Possible values are:
      `YES`,
      `NO`,
      `DK`,
      `NA`
    """,
    example = "NO",
    allowableValues = ["YES", "NO", "DK", "NA"],
  )
  val previous: String? = null,
  @Schema(
    description = "Supporting comments for any previous concerns.",
    example = "Risk of self harm concerns due to ...",
  )
  val previousConcernsText: String? = null,
  @Schema(
    description = """
      Current concerns. Possible values are:
      `YES`,
      `NO`,
      `DK`,
      `NA`
    """,
    example = "YES",
    allowableValues = ["YES", "NO", "DK", "NA"],
  )
  val current: String? = null,
  @Schema(
    description = "Supporting comments for any current concerns.",
    example = "Risk of self harm concerns due to ...",
  )
  val currentConcernsText: String? = null,
)
