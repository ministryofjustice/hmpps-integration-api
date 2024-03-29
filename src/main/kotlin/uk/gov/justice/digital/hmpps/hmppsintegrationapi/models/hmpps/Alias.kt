package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDate

data class Alias(
  val firstName: String,
  @JsonAlias("surname")
  val lastName: String,
  @JsonAlias("middleNames")
  val middleName: String? = null,
  @JsonAlias("dob")
  var dateOfBirth: LocalDate? = null,
  val gender: String? = null,
  val ethnicity: String? = null,
)
