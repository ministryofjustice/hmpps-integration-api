package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import java.time.LocalDate

data class EducationCourseCompletionRequest(
  @field:NotNull(message = "courseCompletion must not be null")
  @field:Valid
  val courseCompletion: CourseCompletion,
)

data class CourseCompletion(
  @Schema(description = "External course reference")
  val externalReference: String,
  @field:NotNull(message = "person must not be null")
  @field:Valid
  val person: PersonDetails,
  @field:NotNull(message = "course must not be null")
  @field:Valid
  val course: CourseDetails,
)

data class PersonDetails(
  @Schema(description = "First name of the Person on Probation (PoP)")
  @field:NotBlank(message = "firstName must not be null or blank")
  val firstName: String,
  @Schema(description = "Last name of the Person on Probation (PoP)")
  @field:NotBlank(message = "lastName must not be null or blank")
  val lastName: String,
  @Schema(description = "Date of birth of the Person on Probation (PoP)", example = "1990-01-01")
  @field:NotNull(message = "dateOfBirth must not be null or blank")
  val dateOfBirth: LocalDate,
  @Schema(description = "Region of the Person on Probation (PoP)")
  @field:NotBlank(message = "region must not be null or blank")
  val region: String,
  @Schema(description = "The office that the Person on Probation (PoP) is associated with")
  @field:NotBlank(message = "office must not be null or blank")
  val office: String,
  @Schema(description = "Email address of the Person on Probation (PoP)")
  @field:NotBlank(message = "email must not be null or blank")
  @field:Pattern(
    regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
    message = "email must be a valid email address",
  )
  val email: String,
)

data class CourseDetails(
  @Schema(description = "Name of the course the Person on Probation (PoP) has completed")
  @field:NotBlank(message = "courseName must not be null or blank")
  val courseName: String,
  @Schema(description = "Type of the course the Person on Probation (PoP) has completed")
  @field:NotBlank(message = "courseType must not be null or blank")
  val courseType: String,
  @Schema(description = "Provider of the course")
  @field:NotBlank(message = "provider must not be null or blank")
  val provider: String,
  @Schema(description = "Date when the Person on Probation (PoP) completed the course", example = "2023-12-31")
  @field:NotNull(message = "completionDate must not be null")
  val completionDate: LocalDate,
  @Schema(description = "Status of the course the Person on Probation (PoP) has completed eg. Failed, Completed")
  @field:NotBlank(message = "status must not be null or blank")
  val status: String,
  @Schema(example = "150", description = "Total time spent on the course in minutes")
  @field:Positive(message = "totalTime must be positive")
  val totalTimeMinutes: Long,
  @Schema(description = "Number of attempts the Person on Probation (PoP) has taken", example = "3")
  val attempts: Int?,
  @Schema(description = "Expected minutes for the Person on Probation (PoP) to complete the course", example = "140")
  @field:Positive(message = "expectedTimeMinutes must be positive")
  @field:NotNull(message = "expectedTimeMinutes must not be null")
  val expectedTimeMinutes: Long,
)
