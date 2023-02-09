package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alias
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import java.time.LocalDate

data class Prisoner(
  val firstName: String,
  val lastName: String,
  val middleNames: String?,
  val dateOfBirth: LocalDate? = null,
  val aliases: List<PrisonerAlias> = listOf(),
) {
  fun toPerson(): Person = Person(
    this.firstName,
    this.lastName,
    this.middleNames,
    this.dateOfBirth,
    this.aliases.map {
      Alias(
        it.firstName,
        it.lastName,
        it.middleNames,
        it.dateOfBirth
      )
    }
  )
}
