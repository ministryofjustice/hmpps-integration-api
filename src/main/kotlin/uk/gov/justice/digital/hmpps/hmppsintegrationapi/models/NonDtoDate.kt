package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class NonDtoDate(
  val date: LocalDate? = null,
  val releaseDateType: String? = null,
)