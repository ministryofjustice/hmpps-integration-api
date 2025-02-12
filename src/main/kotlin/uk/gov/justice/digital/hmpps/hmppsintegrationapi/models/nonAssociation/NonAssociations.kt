package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation

import io.swagger.v3.oas.annotations.media.Schema

data class NonAssociations(
  @Schema(description = "List of non-associations")
  val nonAssociations: List<NonAssociation>,
)

data class NonAssociation(
  @Schema(description = "The ID of the non association")
  val id: Int,
  @Schema(description = "The prisoner's role code in the non-association", example = "VICTIM")
  val role: String,
  @Schema(description = "The description of the prisoner's role in the non-association", example = "Victim")
  val roleDescription: String,
  @Schema(description = "The reason code why these prisoners should be kept apart", example = "BULLYING")
  val reason: String,
  @Schema(description = "The reason description why these prisoners should be kept apart", example = "Bullying")
  val reasonDescription: String,
  @Schema(description = "Location-based restriction code", example = "CELL")
  val restrictionType: String,
  @Schema(description = "Location-based restriction description", example = "Cell only")
  val restrictionTypeDescription: String,
  @Schema(description = "Explanation of why prisoners are non-associated", example = "John and Luke always end up fighting")
  val comment: String,
  @Schema(description = "User ID of the person who created the non-association.", example = "OFF3_GEN")
  val authorisedBy: String,
  @Schema(description = "When the non-association was created", example = "2021-12-31T12:34:56.789012")
  val whenCreated: String,
  @Schema(description = "When the non-association was last updated", example = "2021-12-31T12:34:56.789012")
  val whenUpdated: String,
  @Schema(description = "User ID of the person who last updated the non-association")
  val updatedBy: String,
  @Schema(description = "Whether the non-association is closed or is in effect")
  val isClosed: Boolean,
  @Schema(description = "User ID of the person who closed the non-association. Only present when the non-association is closed, null for open non-associations")
  val closedBy: String?,
  @Schema(description = "Reason why the non-association was closed. Only present when the non-association is closed, null for open non-associations")
  val closedReason: String?,
  @Schema(description = "Date and time of when the non-association was closed. Only present when the non-association is closed, null for open non-associations")
  val closedAt: String?,
  @Schema(description = "Details about the other person in the non-association.")
  val otherPrisonerDetails: NonAssociationPrisonerDetails,
  @Schema(description = "Whether the non-association is open or closed")
  val isOpen: Boolean,
)

data class NonAssociationPrisonerDetails(
  @Schema(description = "Prisoner number", example = "D5678EF")
  val prisonerNumber: String,
  @Schema(description = "Other prisoner’s role code in the non-association", example = "VICTIM")
  val role: String,
  @Schema(description = "Other prisoner’s role description in the non-association")
  val roleDescription: String,
  @Schema(description = "First name", example = "John")
  val firstName: String,
  @Schema(description = "Last name", example = "Smith")
  val lastName: String,
  @Schema(description = "ID of the prison", example = "MDI")
  val prisonId: String,
  @Schema(description = "Name of the prison", example = "Moorland")
  val prisonName: String,
  @Schema(description = "Cell the prisoner is assigned to", example = "B-2-007")
  val cellLocation: String,
)
