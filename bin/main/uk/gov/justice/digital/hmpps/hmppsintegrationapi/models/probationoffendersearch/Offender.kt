package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import java.time.LocalDate

data class Offender(
  val firstName: String,
  val surname: String,
  val middleNames: List<String> = listOf(),
  val dateOfBirth: LocalDate? = null,
  val gender: String? = null,
  val offenderProfile: OffenderProfile = OffenderProfile(),
  val offenderAliases: List<OffenderAlias> = listOf(),
  val contactDetails: ContactDetails? = ContactDetails(),
  val otherIds: OtherIds = OtherIds(),
) {
  fun toPerson(): Person = Person(
    firstName = this.firstName,
    lastName = this.surname,
    middleName = this.middleNames.joinToString(" ").ifEmpty { null },
    dateOfBirth = this.dateOfBirth,
    gender = this.gender,
    ethnicity = this.offenderProfile.ethnicity,
    aliases = this.offenderAliases.map { it.toAlias() },
    identifiers = Identifiers(
      nomisNumber = otherIds.nomsNumber,
      croNumber = otherIds.croNumber,
      deliusCrn = otherIds.crn,
    ),
    pncId = otherIds.pncNumber,
  )
}
