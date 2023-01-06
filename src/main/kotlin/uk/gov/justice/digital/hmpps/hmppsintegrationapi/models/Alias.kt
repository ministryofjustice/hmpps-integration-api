package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonSetter
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

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
) {
  var dateOfBirth: LocalDate? = null
    @JsonGetter("dateOfBirth") get
    @JsonSetter("dob") set
}
