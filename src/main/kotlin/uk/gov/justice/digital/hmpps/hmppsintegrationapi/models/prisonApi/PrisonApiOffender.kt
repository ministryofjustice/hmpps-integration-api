package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Alias
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import java.time.LocalDate

data class PrisonApiOffender(
  val firstName: String,
  val lastName: String,
  val middleName: String? = null,
  val dateOfBirth: LocalDate? = null,
  val aliases: List<PrisonApiAlias> = listOf(),
) {
  fun toPerson(): Person =
    Person(
      firstName = this.firstName,
      lastName = this.lastName,
      middleName = this.middleName,
      dateOfBirth = this.dateOfBirth,
      aliases =
        this.aliases.map {
          Alias(
            it.firstName,
            it.lastName,
            it.middleName,
            it.dob,
          )
        },
    )
}
