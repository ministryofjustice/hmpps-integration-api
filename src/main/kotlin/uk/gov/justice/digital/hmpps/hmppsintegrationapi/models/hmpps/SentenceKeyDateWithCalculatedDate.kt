package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.time.LocalDate

data class SentenceKeyDateWithCalculatedDate(
  val date: LocalDate? = null,
  val overrideDate: LocalDate? = null,
  val calculatedDate: LocalDate? = null,
)
