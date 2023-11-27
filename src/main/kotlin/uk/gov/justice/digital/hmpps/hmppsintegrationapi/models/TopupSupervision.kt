package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class TopupSupervision(
  val expiryCalculatedDate: LocalDate? = null,
  val expiryDate: LocalDate? = null,
  val expiryOverrideDate: LocalDate? = null,
  val startDate: LocalDate? = null,
)
