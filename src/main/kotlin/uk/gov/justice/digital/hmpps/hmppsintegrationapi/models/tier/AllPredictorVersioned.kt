package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier

import java.time.LocalDateTime

data class AllPredictorVersioned(
  val completedDate: LocalDateTime? = null,
  val assessmentType: String? = null,
  val outputVersion: String? = null,
  val output: AllPredictorDto? = null,
)
