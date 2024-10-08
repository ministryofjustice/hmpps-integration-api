package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.filters

import java.time.LocalDateTime

class CaseNoteFilter(
  val hmppsId: String,
  val startDate: LocalDateTime? = null,
  val endDate: LocalDateTime? = null,
  val locationId: String? = null,
)
