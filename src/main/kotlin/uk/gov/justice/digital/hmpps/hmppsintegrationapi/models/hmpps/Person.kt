package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDate

data class Person(
  val firstName: String,
  @JsonAlias("surname")
  val lastName: String,
  @JsonAlias("middleNames")
  val middleName: String? = null,
  val dateOfBirth: LocalDate? = null,
  val gender: String? = null,
  val ethnicity: String? = null,
  @JsonAlias("offenderAliases")
  val aliases: List<Alias> = listOf(),
  val identifiers: Identifiers = Identifiers(),
  val pncId: String? = null,
  val hmppsId: String? = null,
)
