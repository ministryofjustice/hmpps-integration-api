package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class Sentence(
  val startDate: LocalDate? = null,
  val days: Int? = null,
  val weeks: Int? = null,
  val months: Int? = null,
  val years: Int? = null,
  val fineAmount: Double? = null,
  val isLifeSentence: Boolean?,
)
