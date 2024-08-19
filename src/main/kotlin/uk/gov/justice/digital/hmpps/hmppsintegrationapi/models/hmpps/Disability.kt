package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

class Disability(
  val disabilityType: KeyValue? = null,
  val condition: KeyValue? = null,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  @Schema(example = "Walking issue")
  val notes: String? = null,
)
