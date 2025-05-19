package uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers

val personInNomisOnlyPersona =
  Persona(
    firstName = "Joe",
    lastName = "Bloggs",
    identifiers =
      Identifiers(
        nomisNumber = "A1234BC",
      ),
  )
