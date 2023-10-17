package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import java.time.LocalDate

data class Alias(
  val firstName: String,
  val lastName: String,
  val middleName: String? = null,
  var dob: LocalDate? = null,
)
