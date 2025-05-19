package uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactDetailsWithEmailAndPhone
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PhoneNumber

val personInProbationAndNomisPersona =
  Persona(
    firstName = "John",
    lastName = "Doe",
    contactDetails =
      ContactDetailsWithEmailAndPhone(
        phoneNumbers =
          listOf(
            PhoneNumber("07XXXXXXXXX", "Mobile"),
            PhoneNumber("01611XXXXXX", "Landline"),
          ),
        emails =
          listOf(
            "john.doe@test.com",
          ),
      ),
    identifiers =
      Identifiers(
        nomisNumber = "G2996UX",
        deliusCrn = "CD123123",
      ),
  )
