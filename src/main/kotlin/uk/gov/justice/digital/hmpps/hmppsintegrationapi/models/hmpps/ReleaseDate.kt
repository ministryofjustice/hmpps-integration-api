package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.time.LocalDate

data class ReleaseDate(
  val date: LocalDate? = null,
  val confirmedDate: LocalDate? = null,
)
