package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
data class ReviewSchedule(
  @Schema(
    description = "An ISO-8601 date representing when the Review should be completed by.",
    example = "2023-09-01",
  )
  val deadlineDate: LocalDate? = null,
  val nomisNumber: String? = null,
  val description: String? = null,
)
