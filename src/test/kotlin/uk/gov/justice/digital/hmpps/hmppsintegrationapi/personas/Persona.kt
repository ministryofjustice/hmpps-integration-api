package uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers

data class Persona(
  val firstName: String,
  val lastName: String,
  val identifiers: Identifiers,
)
