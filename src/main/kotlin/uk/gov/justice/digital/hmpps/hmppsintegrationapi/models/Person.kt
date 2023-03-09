package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDate

data class Person(
  val firstName: String,
  @JsonAlias("surname")
  val lastName: String,
  @JsonAlias("middleNames")
  val middleName: String? = null,
  val dateOfBirth: LocalDate? = null,
  @JsonAlias("offenderAliases")
  val aliases: List<Alias> = listOf(),
  val prisonerId: String? = null
)
