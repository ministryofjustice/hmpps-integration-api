package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import java.time.LocalDate

data class OffenderAlias(
  val firstName: String,
  val lastName: String,
  val middleName: String,
  var dob: LocalDate? = null
)
