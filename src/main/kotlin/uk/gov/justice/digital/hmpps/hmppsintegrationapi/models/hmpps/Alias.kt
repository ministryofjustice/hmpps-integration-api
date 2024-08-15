package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonAlias
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class Alias(
  @Schema(description = "first name", example = "John")
  val firstName: String,
  @JsonAlias("surname")
  @Schema(description = "last name", example = "Marston")
  val lastName: String,
  @JsonAlias("middleNames")
  @Schema(description = "last name", example = "Marston")
  val middleName: String? = null,
  @JsonAlias("dob")
  @Schema(description = "date of birth", example = "1965-12-01")
  var dateOfBirth: LocalDate? = null,
  @Schema(description = "gender", example = "Male")
  val gender: String? = null,
  @Schema(description = "ethnicity", example = "Prefer not to say")
  val ethnicity: String? = null,
)
