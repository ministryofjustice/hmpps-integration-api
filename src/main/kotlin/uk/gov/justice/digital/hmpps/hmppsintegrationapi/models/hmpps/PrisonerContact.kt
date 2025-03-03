package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class PrisonerContact(
  @Schema(description = "The details of a contact as an individual")
  val contact: Contact,
  @Schema(description = "The details of the relationship between the prisoner and this contact")
  val relationship: PrisonerContactRelationship,
)
