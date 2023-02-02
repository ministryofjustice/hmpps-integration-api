package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDate

data class Person(
  val firstName: String,
  val lastName: String,
  @JsonAlias("middleNames")
  val middleName: String? = null,
  val dateOfBirth: LocalDate? = null,
  val aliases: List<Alias> = listOf()
)
