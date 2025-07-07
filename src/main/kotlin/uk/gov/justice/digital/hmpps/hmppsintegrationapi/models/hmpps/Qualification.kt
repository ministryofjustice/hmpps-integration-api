package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class Qualification(
  @Schema(description = "The subject of the qualification.", example = "Maths GCSE", required = true)
  val subject: String,
  @Schema(description = "The level of an educational qualification.", examples = [ "ENTRY_LEVEL", "LEVEL_1", "LEVEL_2", "LEVEL_3", "LEVEL_4", "LEVEL_5", "LEVEL_6", "LEVEL_7", "LEVEL_8" ], required = true)
  val level: String,
  @Schema(description = "The grade which was achieved (if known/relevant). Note: This is a free format value and there is no type or enum. Therefore values can be \"A\", \"B\", \"C\" etc, but also \"1\", \"2\", \"3\", \"Pass\", \"Distinction\", \"Merit\", \"First class honours\" etc. It is up to the consumer to interpret this data as necessary.", required = true)
  val grade: String,
)
