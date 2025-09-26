package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class Location(
  @Schema(description = "Business Key for a location", example = "MDI")
  val key: String,
  @Schema(description = "Location Code", example = "001")
  val code: String,
  @Schema(description = "Full path of the location within the prison", example = "A-1-001")
  val pathHierarchy: String,
  @Schema(description = "Location Type", example = "CELL")
  val locationType: String,
  @Schema(description = "Alternative description to display for location, (Not Cells)", example = "Wing A")
  val localName: String?,
  @Schema(description = "Additional comments that can be made about this location", example = "Not to be used")
  val comments: String?,
  @Schema(description = "Capacity details of the location")
  val capacity: LocationCapacity?,
  @Schema(description = "When a cell is inactive, show the active working capacity value")
  val oldWorkingCapacity: Int?,
  @Schema(description = "Indicates that this location is certified for use as a residential location")
  val certification: LocationCertification?,
  @Schema(description = "Non Residential Usage")
  val usage: List<LocationUsageItem>?,
  @Schema(description = "Accommodation Types", example = "CARE_AND_SEPARATION", examples = ["CARE_AND_SEPARATION", "HEALTHCARE_INPATIENTS", "NORMAL_ACCOMMODATION", "OTHER_NON_RESIDENTIAL"])
  val accommodationTypes: List<String>?,
  @Schema(
    description = "Specialist Cell Types",
    example = "ACCESSIBLE_CELL",
    examples = [
      "ACCESSIBLE_CELL",
      "BIOHAZARD_DIRTY_PROTEST",
      "CSU",
      "CAT_A",
      "CONSTANT_SUPERVISION",
      "DRY",
      "ESCAPE_LIST",
      "ISOLATION_DISEASES",
      "LISTENER_CRISIS",
      "LOCATE_FLAT_CELL",
      "MEDICAL",
      "MOTHER_AND_BABY",
      "SAFE_CELL",
      "UNFURNISHED",
    ],
  )
  val specialistCellTypes: List<String>?,
  @Schema(
    description = "Used For",
    example = "CLOSE_SUPERVISION_CENTRE",
    examples = [
      "CLOSE_SUPERVISION_CENTRE",
      "SUB_MISUSE_DRUG_RECOVERY",
      "FIRST_NIGHT_CENTRE",
      "HIGH_SECURITY",
      "IPP_LONG_TERM_SENTENCES",
      "MOTHER_AND_BABY",
      "OPEN_UNIT",
      "PATHWAY_TO_PROG",
      "PERINATAL_UNIT",
      "PERSONALITY_DISORDER",
      "PIPE",
      "REMAND",
      "SEPARATION_CENTRE",
      "STANDARD_ACCOMMODATION",
      "THERAPEUTIC_COMMUNITY",
      "VULNERABLE_PRISONERS",
      "YOUNG_PERSONS",
    ],
  )
  val usedFor: List<String>?,
  @Schema(description = "Status of the location", example = "ACTIVE", examples = ["ACTIVE", "INACTIVE", "NON_RESIDENTIAL", "ARCHIVED"])
  val status: String?,
  @Schema(
    description = "Converted Cell Type",
    example = "HOLDING_ROOM",
    examples = [
      "HOLDING_ROOM",
      "INTERVIEW_ROOM",
      "KITCHEN_SERVERY",
      "LISTENERS_ROOM",
      "OFFICE",
      "SHOWER",
      "STAFF_ROOM",
      "STORE",
      "TREATMENT_ROOM",
      "UTILITY_ROOM",
      "OTHER",
    ],
  )
  val convertedCellType: String?,
  @Schema(description = "Convert Cell Type (Other)")
  val otherConvertedCellType: String?,
  @Schema(description = "Indicates the location is enabled", example = "true")
  val active: Boolean,
  @Schema(description = "Indicates the location in inactive as a parent is deactivated", example = "false")
  val deactivatedByParent: Boolean,
  @Schema(description = "Date the location was deactivated", example = "2023-01-23T12:23:00")
  val deactivatedDate: String?,
  @Schema(
    description = "Reason for deactivation",
    example = "DAMAGED",
    examples = [
      "DAMAGED",
      "DAMP",
      "MAINTENANCE",
      "MOTHBALLED",
      "PEST",
      "REFURBISHMENT",
      "SECURITY_SEALED",
      "STAFF_SHORTAGE",
      "OTHER",
    ],
  )
  val deactivatedReason: String?,
  @Schema(description = "For OTHER deactivation reason, a free text comment is provided", example = "Window damaged")
  val deactivationReasonDescription: String?,
  @Schema(description = "Staff username who deactivated the location")
  val deactivatedBy: String?,
  @Schema(description = "Estimated reactivation date for location reactivation", example = "2026-01-24")
  val proposedReactivationDate: String?,
  @Schema(description = "External reference")
  val externalReference: String?,
  @Schema(description = "Current Level within hierarchy, starts at 1, e.g Wing = 1", example = "1", examples = ["1", "2", "3"])
  val level: Int,
  @Schema(description = "Business Key for the parent location", example = "MDI")
  val parentLocationKey: String?,
  @Schema(description = "Number of inactive cells below this location")
  val inactiveCells: Int?,
  @Schema(description = "Total number of non-structural locations are below this level, e.g. cells and rooms")
  val numberOfCellLocations: Int?,
  @Schema(description = "Indicates if the location is a residential location", example = "true")
  val isResidential: Boolean,
)
