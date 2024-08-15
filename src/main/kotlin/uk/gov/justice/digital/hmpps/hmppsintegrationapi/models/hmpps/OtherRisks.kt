package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class OtherRisks(
  @Schema(
    description = """
      Risk of escape/abscond. Possible values are:
      `YES`,
      `NO`,
      `DK`,
      `NA`
    """,
    example = "YES",
    allowableValues = ["YES", "NO", "DK", "NA"],
  )
  val escapeOrAbscond: String? = null,
  @Schema(
    description = """
      Risk control issues/disruptive behaviour. Possible values are:
      `YES`,
      `NO`,
      `DK`,
      `NA`
    """,
    example = "DK",
    allowableValues = ["YES", "NO", "DK", "NA"],
  )
  val controlIssuesDisruptiveBehaviour: String? = null,
  @Schema(
    description = """
      Risk of breach of trust. Possible values are:
      `YES`,
      `NO`,
      `DK`,
      `NA`
    """,
    example = "NO",
    allowableValues = ["YES", "NO", "DK", "NA"],
  )
  val breachOfTrust: String? = null,
  @Schema(
    description = """
      Risk to other prisoners. Possible values are:
      `YES`,
      `NO`,
      `DK`,
      `NA`
    """,
    example = "YES",
    allowableValues = ["YES", "NO", "DK", "NA"],
  )
  val riskToOtherPrisoners: String? = null,
)
