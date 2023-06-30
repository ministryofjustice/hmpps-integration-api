package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import java.time.LocalDate

data class Prisoner(
  val firstName: String,
  val lastName: String,
  val middleNames: String? = null,
  val dateOfBirth: LocalDate? = null,
  val gender: String? = null,
  val ethnicity: String? = null,
  val aliases: List<PrisonerAlias> = listOf(),
  val prisonerNumber: String? = null,
  val pncNumber: String? = null,
  val croNumber: String? = null,
) {
  fun toPerson(): Person = Person(
    firstName = this.firstName,
    lastName = this.lastName,
    middleName = this.middleNames,
    dateOfBirth = this.dateOfBirth,
    gender = this.gender,
    ethnicity = this.ethnicity,
    aliases = this.aliases.map { it.toAlias() },
    identifiers = Identifiers(
      nomisNumber = this.prisonerNumber,
      croNumber = this.croNumber,
    ),
    pncId = this.pncNumber,
  )
}
