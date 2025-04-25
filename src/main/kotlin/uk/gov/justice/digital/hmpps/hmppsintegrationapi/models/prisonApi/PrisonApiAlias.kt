package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

import java.time.LocalDate

data class PrisonApiAlias(
  val firstName: String,
  val lastName: String,
  val middleName: String? = null,
  var dob: LocalDate? = null,
)
