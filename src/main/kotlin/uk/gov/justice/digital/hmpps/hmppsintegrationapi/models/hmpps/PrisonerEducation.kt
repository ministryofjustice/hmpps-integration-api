package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class PrisonerEducation(
  @Schema(description = "An enumeration of education levels.", examples = [ "PRIMARY_SCHOOL", "SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS", "SECONDARY_SCHOOL_TOOK_EXAMS", "FURTHER_EDUCATION_COLLEGE", "UNDERGRADUATE_DEGREE_AT_UNIVERSITY", "POSTGRADUATE_DEGREE_AT_UNIVERSITY", "NO_FORMAL_EDUCATION", "NOT_SURE" ], required = true)
  val educationLevel: String,
  @Schema(description = "A list of achieved qualifications. Can be empty but not null.", required = true)
  val qualifications: List<Qualification>,
)
