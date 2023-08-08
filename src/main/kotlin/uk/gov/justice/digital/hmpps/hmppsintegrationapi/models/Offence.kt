package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class Offence(
  val cjsCode: String? = null,
  val hoCode: String? = null,
  val courtDates: List<LocalDate?> = listOf(),
  val description: String? = null,
  val endDate: LocalDate? = null,
  val startDate: LocalDate? = null,
  val statuteCode: String? = null,
)
