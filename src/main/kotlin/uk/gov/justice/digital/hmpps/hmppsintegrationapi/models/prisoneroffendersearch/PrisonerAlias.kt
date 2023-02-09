package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

import java.time.LocalDate

data class PrisonerAlias(
  val firstName: String,
  val lastName: String,
  val middleNames: String? = null,
  var dateOfBirth: LocalDate? = null
)
