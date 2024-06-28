package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonIntegrationpes.PESPrisonerDetails
import java.time.LocalDate

data class POSPrisoner(
  val firstName: String,
  val lastName: String,
  val middleNames: String? = null,
  val dateOfBirth: LocalDate? = null,
  val gender: String? = null,
  val ethnicity: String? = null,
  val aliases: List<POSPrisonerAlias> = listOf(),
  val prisonerNumber: String? = null,
  val pncNumber: String? = null,
  val bookingId: String? = null,
  val maritalStatus: String? = null,
  val croNumber: String? = null,
  val prisonId: String? = null,
  val prisonName: String? = null,
  val cellLocation: String? = null,
) {
  fun toPerson(): Person =
    Person(
      firstName = this.firstName,
      lastName = this.lastName,
      middleName = this.middleNames,
      dateOfBirth = this.dateOfBirth,
      gender = this.gender,
      ethnicity = this.ethnicity,
      aliases = this.aliases.map { it.toAlias() },
      identifiers =
        Identifiers(
          nomisNumber = this.prisonerNumber,
          croNumber = this.croNumber,
        ),
      pncId = this.pncNumber,
    )

  fun toPESPrisonerDetails(): PESPrisonerDetails =
    PESPrisonerDetails(
      prisonerNumber = this.prisonerNumber,
      firstName = this.firstName,
      lastName = this.lastName,
      prisonId = this.prisonId,
      prisonName = this.prisonName,
      cellLocation = this.cellLocation,
    )
}
