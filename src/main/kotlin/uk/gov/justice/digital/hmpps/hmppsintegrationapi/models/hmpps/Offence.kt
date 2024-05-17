package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.time.LocalDate

data class Offence(
  val serviceSource: UpstreamApi,
  val systemSource: SystemSource,
  val cjsCode: String? = null,
  val hoCode: String? = null,
  val courtDates: List<LocalDate?> = listOf(),
  val courtName: String? = null,
  val description: String? = null,
  val endDate: LocalDate? = null,
  val startDate: LocalDate? = null,
  val statuteCode: String? = null,
)
