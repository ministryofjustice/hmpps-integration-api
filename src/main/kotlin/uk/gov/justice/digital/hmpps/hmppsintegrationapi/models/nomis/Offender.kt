package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alias
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import java.time.LocalDate

data class Offender(
  val firstName: String,
  val lastName: String,
  val middleName: String? = null,
  val dateOfBirth: LocalDate? = null,
  val aliases: List<OffenderAlias> = listOf()
) {
  fun toPerson(): Person = Person(
    this.firstName,
    this.lastName,
    this.middleName,
    this.dateOfBirth,
    this.aliases.map { Alias(it.firstName, it.lastName, it.middleName, it.dob) }
  )
}
