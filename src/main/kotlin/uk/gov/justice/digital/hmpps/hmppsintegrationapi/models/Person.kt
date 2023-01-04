package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import io.swagger.v3.oas.annotations.media.Schema

data class Person(
  @field:Schema(
    description = "First name",
    example = "Arthur",
    type = "string",
  )
  val firstName: String,

  @field:Schema(
    description = "Last name",
    example = "Morgan",
    type = "string",
  )
  val lastName: String,

  val aliases: List<Alias> = listOf()
)

data class Alias(
  @field:Schema(
    description = "First name",
    example = "John",
    type = "string",
  )
  val firstName: String,

  @field:Schema(
    description = "Last name",
    example = "Marston",
    type = "string",
  )
  val lastName: String
)
