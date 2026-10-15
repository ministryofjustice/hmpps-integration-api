package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier

import java.time.LocalDateTime

data class AssessmentSummary(
  val id: Long? = null,
  val completedDate: LocalDateTime? = null,
  val type: String? = null,
  val status: String? = null,
  val sanIndicator: Boolean? = null,
)
