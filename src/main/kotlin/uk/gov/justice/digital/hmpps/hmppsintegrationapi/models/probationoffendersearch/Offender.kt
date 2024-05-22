package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonProtectedCharacteristics
import java.time.LocalDate

data class Offender(
  val firstName: String,
  val surname: String,
  val middleNames: List<String> = listOf(),
  val dateOfBirth: LocalDate? = null,
  val gender: String? = null,
  val offenderProfile: OffenderProfile = OffenderProfile(),
  val offenderAliases: List<OffenderAlias> = listOf(),
  val contactDetails: ContactDetails? = null,
  val otherIds: OtherIds = OtherIds(),
  val age: Number = 0,
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
      contactDetails = this.contactDetails?.toContactDetails(),
    )

  fun toPersonProtectedCharacteristics(): PersonProtectedCharacteristics =
    PersonProtectedCharacteristics(
      this.age,
      this.gender,
      this.offenderProfile.sexualOrientation,
      this.offenderProfile.ethnicity,
      this.offenderProfile.nationality,
      this.offenderProfile.religion,
      this.offenderProfile.disabilities,
    )
}
