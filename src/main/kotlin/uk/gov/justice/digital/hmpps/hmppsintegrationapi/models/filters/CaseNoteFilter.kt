package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.filters

import java.time.LocalDateTime

data class CaseNoteFilter(
  val hmppsId: String,
  val startDate: LocalDateTime? = null,
  val endDate: LocalDateTime? = null,
)
