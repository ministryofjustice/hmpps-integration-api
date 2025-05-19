package uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers

val personInProbationOnlyPersona =
  Persona(
    firstName = "John",
    lastName = "Smith",
    identifiers =
      Identifiers(
        deliusCrn = "AB123123",
      ),
  )
