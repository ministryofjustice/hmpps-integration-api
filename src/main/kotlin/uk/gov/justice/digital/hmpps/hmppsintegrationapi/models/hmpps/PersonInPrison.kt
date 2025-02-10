package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonAlias
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class PersonInPrison(
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
  @Schema(description = "Category", example = "C")
  val category: String? = null,
  @Schema(description = "Cell sharing risk assessment", example = "HIGH")
  val csra: String? = null,
  @Schema(description = "Date prisoner was received into the prison", example = "2021-12-01")
  val receptionDate: String? = null,
  @Schema(description = "Status of the prisoner", example = "ACTIVE IN")
  val status: String? = null,
  @Schema(description = "Prison ID", example = "MDI")
  val prisonId: String? = null,
  @Schema(description = "Name of the prison", example = "HMP Leeds")
  val prisonName: String? = null,
  @Schema(description = "In prison cell location", example = "A-1-002")
  val cellLocation: String? = null,
)
