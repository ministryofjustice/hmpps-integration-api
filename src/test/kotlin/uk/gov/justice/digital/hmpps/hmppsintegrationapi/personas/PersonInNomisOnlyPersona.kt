package uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactDetailsWithEmailAndPhone
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PhoneNumber

val personInNomisOnlyPersona =
  Persona(
    firstName = "Joe",
    lastName = "Bloggs",
    contactDetails =
      ContactDetailsWithEmailAndPhone(
        phoneNumbers =
          listOf(
            PhoneNumber("07XXXXXXXXX", "Mobile"),
            PhoneNumber("01611XXXXXX", "Landline"),
          ),
        emails =
          listOf(
            "joe.bloggs@test.com",
          ),
      ),
    identifiers =
      Identifiers(
        nomisNumber = "A1234BC",
      ),
  )
