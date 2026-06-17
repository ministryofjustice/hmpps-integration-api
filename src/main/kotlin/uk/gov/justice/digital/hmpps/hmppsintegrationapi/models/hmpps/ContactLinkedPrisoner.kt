package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.interfaces.IPaginatedObject

data class ContactLinkedPrisoner(
  @Schema(description = "The prisoner number", example = "A1234BC")
  val prisonerNumber: String,
  @Schema(description = "The prisoners last name", example = "Smith")
  val lastName: String,
  @Schema(description = "The prisoners first name", example = "John")
  val firstName: String,
  @Schema(description = "The prisoners middle names", example = "Simon James")
  val middleNames: String?,
  @Schema(description = "Relationship type code ", example = "S")
  val relationshipTypeCode: String,
  @Schema(description = "Relationship type description", example = "Official")
  val relationshipTypeDescription: String,
  @Schema(description = "Relationship to prisoner code", example = "FRI")
  val relationshipToPrisonerCode: String,
  @Schema(description = "Relationship to prisoner code description", example = "Friend")
  val relationshipToPrisonerDescription: String?,
  @Schema(description = "Is relationship active?", example = "True")
  val isRelationshipActive: Boolean,
  @Schema(description = "The prisoner contact Id", example = "123456")
  val prisonerContactId: Long,
)

data class PaginatedContactLinkedPrisonerResponse(
  override val content: List<ContactLinkedPrisoner>,
  override val isLastPage: Boolean,
  override val count: Int,
  override val page: Int,
  override val perPage: Int,
  override val totalCount: Long,
  override val totalPages: Int,
) : IPaginatedObject<ContactLinkedPrisoner>
