package uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactDetailsWithEmailAndPhone
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PhoneNumber
import java.time.LocalDate

val personInNomisOnlyPersona =
  Persona(
    firstName = "Joe",
    lastName = "Bloggs",
    dateOfBirth = LocalDate.parse("1992-04-07"),
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
