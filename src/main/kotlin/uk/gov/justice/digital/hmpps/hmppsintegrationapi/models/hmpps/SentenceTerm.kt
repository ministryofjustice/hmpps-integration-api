package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class SentenceTerm(
  @Schema(description = "Number of years in the term", example = "5")
  val years: Int? = null,
  @Schema(description = "Number of months in the term", example = "4")
  val months: Int? = null,
  @Schema(description = "Number of weeks in the term", example = "3")
  val weeks: Int? = null,
  @Schema(description = "Number of days in the term", example = "2")
  val days: Int? = null,
  @Schema(description = "Number of hours in the term", example = "1")
  val hours: Int? = null,
  @Schema(
    description = """
      The sentence term code
      Possible values are:
      `CUR` - Curfew Period,
      `DEF` - Deferment Period,
      `DET` - Detention,
      `HOURS` - Hours Ordered,
      `IMP` - Imprisonment,
      `LIC` - Licence,
      `PSYCH` - Psychiatric Hospital,
      `SCUS` - Custodial Period,
      `SEC104` - Breach of supervision requirements,
      `SEC105` - Breach due to imprisonable offence,
      `SEC86` - Section 86 of 2000 Act,
      `SUP` - Sentence Length,
      `SUSP` - Suspension Period
    """,
    example = "IMP",
    allowableValues = ["CUR", "DEF", "DET", "HOURS", "IMP", "LIC", "PSYCH", "SCUS", "SEC104", "SEC105", "SEC86", "SUP", "SUSP"],
  )
  val prisonTermCode: String? = null,
)
