package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class Offence(
  val cjsCode: String? = null,
  val courtDate: LocalDate? = null,
  val description: String? = null,
  val endDate: LocalDate? = null,
  val startDate: LocalDate? = null,
  val statuteCode: String? = null,
)
