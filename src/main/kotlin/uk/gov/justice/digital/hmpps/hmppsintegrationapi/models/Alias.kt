package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDate

data class Alias(
  val firstName: String,
  val lastName: String,
  @JsonAlias("middleNames")
  val middleName: String? = null,
  @JsonAlias("dob")
  var dateOfBirth: LocalDate? = null
)
