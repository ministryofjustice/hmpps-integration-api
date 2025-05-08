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
  val religion: String,
  val raceCode: String,
  val nationality: String,
) {
  fun toPerson(): Person =
    Person(
      firstName = this.firstName,
      lastName = this.lastName,
      middleName = this.middleName,
      dateOfBirth = this.dateOfBirth,
      religion = this.religion,
      raceCode = this.raceCode,
      nationality = this.nationality,
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
