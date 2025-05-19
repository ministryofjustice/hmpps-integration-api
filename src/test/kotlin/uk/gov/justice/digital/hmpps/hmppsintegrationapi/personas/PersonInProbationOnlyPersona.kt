package uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactDetailsWithEmailAndPhone
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PhoneNumber

val personInProbationOnlyPersona =
  Persona(
    firstName = "John",
    lastName = "Smith",
    contactDetails =
      ContactDetailsWithEmailAndPhone(
        phoneNumbers =
          listOf(
            PhoneNumber("07XXXXXXXXX", "Mobile"),
            PhoneNumber("01611XXXXXX", "Landline"),
          ),
        emails =
          listOf(
            "john.smith@test.com",
          ),
      ),
    identifiers =
      Identifiers(
        deliusCrn = "AB123123",
      ),
  )
