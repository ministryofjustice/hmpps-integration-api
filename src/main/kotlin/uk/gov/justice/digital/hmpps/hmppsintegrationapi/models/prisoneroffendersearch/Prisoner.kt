package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alias
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import java.time.LocalDate

data class Prisoner(
  val firstName: String,
  val lastName: String,
  val middleNames: String? = null,
  val dateOfBirth: LocalDate? = null,
  val aliases: List<PrisonerAlias> = listOf(),
  val prisonerNumber: String? = null
) {
  fun toPerson(): Person = Person(
    firstName = this.firstName,
    lastName = this.lastName,
    middleName = this.middleNames,
    dateOfBirth = this.dateOfBirth,
    aliases = this.aliases.map {
      Alias(
        it.firstName,
        it.lastName,
        it.middleNames,
        it.dateOfBirth
      )
    },
    prisonerId = this.prisonerNumber
  )
}
