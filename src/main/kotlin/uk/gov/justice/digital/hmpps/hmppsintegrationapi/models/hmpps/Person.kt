package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
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
  @Schema(description = "HMPPS identifier", example = "X00001")
  val hmppsId: String? = null,
  val contactDetails: ContactDetailsWithEmailAndPhone? = null,
  val currentRestriction: Boolean? = null,
  val restrictionMessage: String? = null,
  val currentExclusion: Boolean? = null,
  val exclusionMessage: String? = null,
) : LaoIdentifiable {
  fun copy(
    firstName: String = this.firstName,
    lastName: String = this.lastName,
    middleName: String? = this.middleName,
    dateOfBirth: LocalDate? = this.dateOfBirth,
    gender: String? = this.gender,
    ethnicity: String? = this.ethnicity,
    aliases: List<Alias> = this.aliases,
    identifiers: Identifiers = this.identifiers,
    pncId: String? = this.pncId,
    hmppsId: String? = this.hmppsId,
    contactDetails: ContactDetailsWithEmailAndPhone? = this.contactDetails,
    currentRestriction: Boolean? = this.currentRestriction,
    restrictionMessage: String? = this.restrictionMessage,
    currentExclusion: Boolean? = this.currentExclusion,
    exclusionMessage: String? = this.exclusionMessage,
  ) = Person(
    firstName = firstName,
    lastName = lastName,
    middleName = middleName,
    dateOfBirth = dateOfBirth,
    gender = gender,
    ethnicity = ethnicity,
    aliases = aliases,
    identifiers = identifiers,
    pncId = pncId,
    hmppsId = hmppsId,
    contactDetails = contactDetails,
    currentRestriction = currentRestriction,
    restrictionMessage = restrictionMessage,
    currentExclusion = currentExclusion,
    exclusionMessage = exclusionMessage,
  )

  @JsonIgnore
  override fun isLao(): Boolean =
    this.currentExclusion == true ||
      this.currentRestriction == true ||
      this.currentExclusion == null ||
      this.currentRestriction == null
}

fun interface LaoIdentifiable {
  fun isLao(): Boolean
}
