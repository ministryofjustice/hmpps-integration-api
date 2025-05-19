package uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers

val personInProbationAndNomisPersona =
  Persona(
    firstName = "John",
    lastName = "Doe",
    identifiers =
      Identifiers(
        nomisNumber = "G2996UX",
        deliusCrn = "CD123123",
      ),
  )
