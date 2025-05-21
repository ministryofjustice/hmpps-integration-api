package uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactDetailsWithEmailAndPhone
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import java.time.LocalDate

data class Persona(
  val firstName: String,
  val lastName: String,
  val dateOfBirth: LocalDate,
  val contactDetails: ContactDetailsWithEmailAndPhone,
  val identifiers: Identifiers,
)
