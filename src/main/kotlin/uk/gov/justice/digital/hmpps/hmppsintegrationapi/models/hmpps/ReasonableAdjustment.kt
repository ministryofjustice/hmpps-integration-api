package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.time.LocalDate

class ReasonableAdjustment(
  val treatmentCode: String? = null,
  val commentText: String? = null,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val treatmentDescription: String? = null,
)
