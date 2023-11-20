package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import java.time.LocalDate

data class SentenceKeyDate(
  val date: LocalDate? = null,
  val overrideDate: LocalDate? = null,
  val calculatedDate: LocalDate? = null,
)
