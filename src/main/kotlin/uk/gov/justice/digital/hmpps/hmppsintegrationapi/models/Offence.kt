package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class Offence(
  val cjsCode: String,
  val courtDate: LocalDate?,
  val description: String,
  val endDate: LocalDate?,
  val startDate: LocalDate,
  val statuteCode: String,
)
