package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

class PersonInPrison(
  person: Person,
  val category: String?,
  val csra: String?,
  val receptionDate: String?,
  val status: String?,
  val prisonId: String?,
  val prisonName: String?,
  val cellLocation: String?,
) : Person(
    firstName = person.firstName,
    lastName = person.lastName,
    middleName = person.middleName,
    dateOfBirth = person.dateOfBirth,
    gender = person.gender,
    ethnicity = person.ethnicity,
    aliases = person.aliases,
    identifiers = person.identifiers,
    pncId = person.pncId,
    hmppsId = person.hmppsId,
    contactDetails = person.contactDetails,
    currentExclusion = person.currentExclusion,
    exclusionMessage = person.exclusionMessage,
    currentRestriction = person.currentRestriction,
    restrictionMessage = person.restrictionMessage,
  )
