package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import java.time.LocalDate

data class Offender(
  val firstName: String,
  val surname: String,
  val middleNames: List<String> = listOf(),
  val dateOfBirth: LocalDate? = null,
  val gender: String? = null,
  val offenderProfile: OffenderProfile = OffenderProfile(),
  val offenderAliases: List<OffenderAlias> = listOf(),
  val contactDetails: ContactDetailsWithEmailAndPhone? = null,
  val otherIds: OtherIds = OtherIds(),
) {
  fun toPerson(): Person =
    Person(
      firstName = this.firstName,
      lastName = this.surname,
      middleName = this.middleNames.joinToString(" ").ifEmpty { "" },
      dateOfBirth = this.dateOfBirth,
      gender = this.gender,
      ethnicity = this.offenderProfile.ethnicity,
      aliases = this.offenderAliases.map { it.toAlias() },
      identifiers =
      Identifiers(
        nomisNumber = otherIds.nomsNumber,
        croNumber = otherIds.croNumber,
        deliusCrn = otherIds.crn,
      ),
      pncId = otherIds.pncNumber,
      hmppsId = otherIds.crn,
      contactDetails = this.contactDetails?.toContactdetails(),
    )
}
