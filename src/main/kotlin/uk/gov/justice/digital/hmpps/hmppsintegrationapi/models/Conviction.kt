package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class Conviction(
  val date: LocalDate,
  val code: String,
  val description: String,
)
