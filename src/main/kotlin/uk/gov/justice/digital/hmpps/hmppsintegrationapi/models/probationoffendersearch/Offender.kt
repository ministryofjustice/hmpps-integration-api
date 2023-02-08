package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alias
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import java.time.LocalDate

data class Offender(
  val firstName: String,
  val surname: String,
  val middleNames: List<String> = listOf(),
  val dateOfBirth: LocalDate? = null,
  val offenderAliases: List<OffenderAlias> = listOf(),
) {
  fun toPerson(): Person = Person(
    this.firstName,
    this.surname,
    this.middleNames.joinToString(" "),
    this.dateOfBirth,
    this.offenderAliases.map { Alias(it.firstName, it.surname, it.middleNames.joinToString(" "), it.dateOfBirth) }
  )
}
