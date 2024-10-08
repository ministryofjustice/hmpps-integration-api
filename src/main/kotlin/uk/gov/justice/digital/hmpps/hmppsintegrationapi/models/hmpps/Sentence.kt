package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class Sentence(
  @Schema(
    description = """
      Which upstream API service the sentence originates from. Possible values are:
      `NOMIS`,
      `NDELIUS`
    """,
    example = "NOMIS",
    allowableValues = ["NOMIS", "NDELIUS"],
  )
  val serviceSource: UpstreamApi,
  @Schema(
    description = """
      Which upstream API system the sentence originates from. Possible values are:
      `PRISON_SYSTEMS`,
      `PROBATION_SYSTEMS`
    """,
    example = "PROBATION_SYSTEMS",
    allowableValues = ["PRISON_SYSTEMS", "PROBATION_SYSTEMS"],
  )
  val systemSource: SystemSource,
  @Schema(description = "Date of sentencing", example = "2009-09-09")
  val dateOfSentencing: LocalDate? = null,
  @Schema(description = "Description of the sentence", example = "Young Offender Inst - >=12 mths")
  val description: String? = null,
  @Schema(description = "Whether the sentence is active", example = "true")
  val isActive: Boolean? = null,
  @Schema(description = "Whether the sentence is custodial", example = "true")
  val isCustodial: Boolean,
  @Schema(description = "The amount of fine related to the sentence and offence", example = "480.59")
  val fineAmount: Number? = null,
  val length: SentenceLength = SentenceLength(),
)
