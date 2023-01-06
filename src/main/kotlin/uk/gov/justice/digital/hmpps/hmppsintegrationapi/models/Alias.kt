package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

// @JsonAutoDetect(
//  fieldVisibility = JsonAutoDetect.Visibility.ANY,
//  getterVisibility = JsonAutoDetect.Visibility.NONE,
//  setterVisibility = JsonAutoDetect.Visibility.NONE,
//  creatorVisibility = JsonAutoDetect.Visibility.NONE
// )
data class Alias @JvmOverloads constructor(
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

  @JsonProperty("dob")
  val dateOfBirth: LocalDate?
)
