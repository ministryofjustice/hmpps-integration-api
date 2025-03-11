package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class PrisonerContactRestrictions(
  @Schema(description = "Relationship specific restrictions")
  var prisonerContactRestrictions: List<ContactRestriction>? = emptyList(),
  @Schema(description = "Global (estate-wide) restrictions for the contact")
  var contactGlobalRestrictions: List<ContactRestriction>? = emptyList(),
)

data class ContactRestriction(
  @Schema(description = "The restriction code", examples = ["CC", "BAN", "CHILD", "CLOSED", "RESTRICTED", "DIHCON", "NONCON"])
  val restrictionType: String,
  @Schema(description = "The description of the restriction type", example = "Banned")
  val restrictionTypeDescription: String,
  @Schema(description = "Restriction created date", example = "2024-01-01")
  val startDate: String,
  @Schema(description = "Restriction expiry date", example = "2024-01-01")
  val expiryDate: String,
  @Schema(description = "Comments for the restriction", example = "N/A")
  val comments: String,
  @Schema(description = "The username of either the person who created the restriction or the last person to update it if it has been modified", example = "admin")
  val enteredByUsername: String,
  @Schema(description = "The display name of either the person who created the restriction or the last person to update it if it has been modified", example = "John Smith")
  val enteredByDisplayName: String,
  @Schema(description = "User who created the entry", example = "admin")
  val createdBy: String,
  @Schema(description = "Timestamp when the entry was created", example = "2023-09-23T10:15:30")
  val createdTime: String,
  @Schema(description = "User who updated the entry", example = "admin")
  val updatedBy: String,
  @Schema(description = "Timestamp when the entry was updated", example = "2023-09-23T10:15:30")
  val updatedTime: String,
)
