package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonAlias
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

open class Person(
  @Schema(description = "First name", example = "John")
  val firstName: String,
  @JsonAlias("surname")
  @Schema(description = "Last name", example = "Morgan")
  val lastName: String,
  @JsonAlias("middleNames")
  @Schema(description = "Middle name", example = "John")
  val middleName: String? = null,
  @Schema(description = "Date of birth", example = "1965-12-01")
  val dateOfBirth: LocalDate? = null,
  @Schema(description = "Gender", example = "Male")
  val gender: String? = null,
  @Schema(description = "Ethnicity", example = "White: Eng./Welsh/Scot./N.Irish/British")
  val ethnicity: String? = null,
  @JsonAlias("offenderAliases")
  val aliases: List<Alias> = listOf(),
  val identifiers: Identifiers = Identifiers(),
  @Schema(description = "An identifier from the Police National Computer (PNC)")
  val pncId: String? = null,
  @Schema(description = "HMPPS identifier", example = "2008/0545166T")
  val hmppsId: String? = null,
  val contactDetails: ContactDetailsWithEmailAndPhone? = null,
  val currentRestriction: Boolean? = null,
  val restrictionMessage: String? = null,
  val currentExclusion: Boolean? = null,
  val exclusionMessage: String? = null,
)
