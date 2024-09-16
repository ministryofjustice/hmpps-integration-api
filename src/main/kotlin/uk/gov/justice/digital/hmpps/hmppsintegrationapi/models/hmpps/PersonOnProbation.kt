package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

class PersonOnProbation(
  person: Person,
  val underActiveSupervision: Boolean,
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
  )
