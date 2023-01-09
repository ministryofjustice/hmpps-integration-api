package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import com.fasterxml.jackson.annotation.JsonAlias
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class Alias(
  @field:Schema(
    description = "First name",
    example = "John",
    type = "string"
  )
  val firstName: String,

  @field:Schema(
    description = "Last name",
    example = "Marston",
    type = "string"
  )
  val lastName: String,

  val middleName: String? = null,

  @JsonAlias("dob")
  var dateOfBirth: LocalDate? = null
)
