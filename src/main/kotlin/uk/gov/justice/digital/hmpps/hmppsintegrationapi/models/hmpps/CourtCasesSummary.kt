package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.time.LocalDate

data class CourtCasesSummary(
  val dateOfFirstConviction: LocalDate? = null,
)
