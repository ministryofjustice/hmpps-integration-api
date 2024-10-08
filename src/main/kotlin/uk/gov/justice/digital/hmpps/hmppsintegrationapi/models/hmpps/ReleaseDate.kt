package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class ReleaseDate(
  @Schema(
    description = """
      Confirmed, actual, approved, provisional or calculated release date for offender, according to offender release date algorithm.

      Algorithm
      If there is a confirmed release date, the offender release date is the confirmed release date.
      If there is no confirmed release date for the offender, the offender release date is either the actual parole date or the home detention curfew actual date.
      If there is no confirmed release date, actual parole date or home detention curfew actual date for the offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both are present)
    """,
    example = "2023-03-01",
  )
  val date: LocalDate? = null,
  @Schema(description = "Confirmed release date for offender.", example = "2023-03-01")
  val confirmedDate: LocalDate? = null,
)
