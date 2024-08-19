package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class SentenceLength(
  @Schema(description = "Duration of the sentence", example = "10")
  val duration: Int? = null,
  @Schema(
    description = """
      Time unit that is used in combination with the duration field. Possible values are:
      `Hours`,
      `Days`,
      `Weeks`,
      `Months`,
      `Years`
    """,
    example = "Hours",
    allowableValues = ["Hours", "Days", "Weeks", "Months", "Years"],
  )
  val units: String? = null,
  val terms: List<SentenceTerm> = emptyList(),
)
