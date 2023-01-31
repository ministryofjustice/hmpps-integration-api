package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class Person(
  val firstName: String,
  val lastName: String,
  val middleName: String? = null,
  val dateOfBirth: LocalDate? = null,
  val aliases: List<Alias> = listOf()
)
