package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Language
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonalCareNeed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PhysicalCharacteristics
import java.time.LocalDate

data class POSPrisoner(
  val prisonerNumber: String? = null,
  val pncNumber: String? = null,
  val croNumber: String? = null,
  val bookingId: String? = null,
  val firstName: String,
  val middleNames: String? = null,
  val lastName: String,
  val dateOfBirth: LocalDate? = null,
  val gender: String? = null,
  val ethnicity: String? = null,
  val youthOffender: Boolean,
  val maritalStatus: String? = null,
  val smoker: String? = null,
  val status: String? = null,
  val lastMovementTypeCode: String? = null,
  val inOutStatus: String? = null,
  val prisonId: String? = null,
  val lastPrisonId: String? = null,
  val prisonName: String? = null,
  val cellLocation: String? = null,
  val aliases: List<POSPrisonerAlias> = listOf(),
  val csra: String? = null,
  val category: String? = null,
  val receptionDate: String? = null,
  val heightCentimetres: Int? = null,
  val weightKilograms: Int? = null,
  val hairColour: String? = null,
  val rightEyeColour: String? = null,
  val leftEyeColour: String? = null,
  val facialHair: String? = null,
  val shapeOfFace: String? = null,
  val build: String? = null,
  val shoeSize: Int? = null,
  val tattoos: List<POSBodyMark>? = null,
  val scars: List<POSBodyMark>? = null,
  val marks: List<POSBodyMark>? = null,
  val personalCareNeeds: List<POSPersonalCareNeed>? = null,
  val languages: List<POSLanguage>? = null,
  val identifiers: List<POSIdentifier>? = null,
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

  fun toPersonInPrison(): PersonInPrison =
    PersonInPrison(
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
      cellLocation = this.cellLocation,
      prisonId = this.prisonId,
      prisonName = this.prisonName,
      category = this.category,
      csra = this.csra,
      receptionDate = this.receptionDate,
      status = this.status,
      youthOffender = this.youthOffender,
    )

  fun toPhysicalCharacteristics(): PhysicalCharacteristics =
    PhysicalCharacteristics(
      heightCentimetres = this.heightCentimetres,
      weightKilograms = this.weightKilograms,
      hairColour = this.hairColour,
      rightEyeColour = this.rightEyeColour,
      leftEyeColour = this.leftEyeColour,
      facialHair = this.facialHair,
      shapeOfFace = this.shapeOfFace,
      build = this.build,
      shoeSize = this.shoeSize,
      tattoos = this.tattoos?.map { it.toBodyMark() },
      scars = this.scars?.map { it.toBodyMark() },
      marks = this.marks?.map { it.toBodyMark() },
    )

  fun toPersonalCareNeeds(): List<PersonalCareNeed>? = this.personalCareNeeds?.map { it.toPersonalCareNeed() }

  fun toLanguages(): List<Language>? = this.languages?.map { it.toLanguage() }
}
