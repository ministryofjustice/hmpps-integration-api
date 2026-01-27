package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebMvcTestConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourseCompletion
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourseDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationCourseCompletionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.EducationCourseCompletionService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDate

@WebMvcTest(controllers = [EducationCourseCompletionController::class])
@Import(WebMvcTestConfiguration::class)
@ActiveProfiles("test")
class EducationCourseControllerTest(
  private val springMockMvc: MockMvc,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val educationCourseCompletionService: EducationCourseCompletionService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val apiPath = "/v1/education/course-completion"

      val educationCourseCompletionRequest =
        EducationCourseCompletionRequest(
          courseCompletion =
            CourseCompletion(
              externalReference = "CC123",
              person =
                PersonDetails(
                  crn = "X123456",
                  firstName = "John",
                  lastName = "Doe",
                  dateOfBirth = LocalDate.parse("1990-01-01"),
                  region = "London",
                  email = "john.doe@example.com",
                ),
              course =
                CourseDetails(
                  courseName = "Test Course",
                  courseType = "Test course type",
                  provider = "Moodle",
                  status = "Completed",
                  totalTime = "02:30",
                  attempts = 1,
                  expectedMinutes = 2.0,
                ),
            ),
        )

      whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.NORMALISED_PATH_MATCHING)).thenReturn(true)
      whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.COURSE_COMPLETION_EVENT)).thenReturn(true)

      describe("Notify that a given person/offender has completed all relevant education assessments") {
        it("returns 202 Accepted and calls the service") {
          val response = mockMvc.performAuthorisedPost(apiPath, educationCourseCompletionRequest).response

          response.status.shouldBe(HttpStatus.ACCEPTED.value())
          verify(educationCourseCompletionService).recordCourseCompletion(any())
        }

        it("returns 403 Forbidden if not authorised") {
          val response =
            springMockMvc
              .perform(
                MockMvcRequestBuilders
                  .post(apiPath),
              ).andReturn()
              .response

          response.status.shouldBe(HttpStatus.FORBIDDEN.value())
        }
      }
    },
  )
