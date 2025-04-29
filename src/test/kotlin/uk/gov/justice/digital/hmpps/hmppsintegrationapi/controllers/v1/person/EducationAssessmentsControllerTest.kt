package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc

@WebMvcTest(controllers = [EducationAssessmentsController::class])
@ActiveProfiles("test")
class EducationAssessmentsControllerTest(
  private val springMockMvc: MockMvc,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
) : DescribeSpec({
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    val validHmppsId = "A1234BC"

    fun apiPath(hmppsId: String = validHmppsId) = "/v1/persons/$hmppsId/education/assessments/status"

    describe("Notify that a given person/offender has had a change of status to their Education Assessments") {
      it("should return 200 given a valid request body") {
        // Given
        val requestBody =
          mapOf(
            "status" to "ALL_RELEVANT_ASSESSMENTS_COMPLETE",
            "statusChangeDate" to "2025-04-22",
            "detailUrl" to "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/A1234AB",
            "requestId" to "0650ba37-a977-4fbe-9000-4715aaecadba",
          )

        val response = mockMvc.performAuthorisedPost(apiPath(), requestBody).response

        // Then
        response.status.shouldBe(HttpStatus.OK.value())
        response.contentAsString.shouldBe("")
      }

      it("should return 400 given missing statusChangeDate") {
        // Given
        val requestBody =
          mapOf(
            "status" to "ALL_RELEVANT_ASSESSMENTS_COMPLETE",
            "statusChangeDate" to null,
            "detailUrl" to "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/A1234AB",
            "requestId" to "0650ba37-a977-4fbe-9000-4715aaecadba",
          )

        // When
        val response = mockMvc.performAuthorisedPost(apiPath(), requestBody).response

        // Then
        response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        val errorResponse = response.contentAsJson<ErrorResponse>()
        assertThat(errorResponse.status).isEqualTo(400)
        assertThat(errorResponse.userMessage).isEqualTo("JSON parse error: Instantiation of [simple type, class uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationAssessmentStatusChangeRequest] value failed for JSON property statusChangeDate due to missing (therefore NULL) value for creator parameter statusChangeDate which is a non-nullable type")
      }

      it("should return 400 given invalid status") {
        // Given
        val requestBody =
          mapOf(
            "status" to "some-invalid-status",
            "statusChangeDate" to "2025-04-22",
            "detailUrl" to "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/A1234AB",
            "requestId" to "0650ba37-a977-4fbe-9000-4715aaecadba",
          )

        // When
        val response = mockMvc.performAuthorisedPost(apiPath(), requestBody).response

        // Then
        response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        val errorResponse = response.contentAsJson<ErrorResponse>()
        assertThat(errorResponse.status).isEqualTo(400)
        assertThat(errorResponse.userMessage).isEqualTo("JSON parse error: Cannot deserialize value of type `uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationAssessmentStatus` from String \"some-invalid-status\": not one of the values accepted for Enum class: [ALL_RELEVANT_ASSESSMENTS_COMPLETE]")
      }

      it("should return 400 given invalid detail url") {
        // Given
        val requestBody =
          mapOf(
            "status" to "ALL_RELEVANT_ASSESSMENTS_COMPLETE",
            "statusChangeDate" to "2025-04-22",
            "detailUrl" to "not-a-valid-url",
            "requestId" to "0650ba37-a977-4fbe-9000-4715aaecadba",
          )

        // When
        val response = mockMvc.performAuthorisedPost(apiPath(), requestBody).response

        // Then
        response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        val errorResponse = response.contentAsJson<ErrorResponse>()
        assertThat(errorResponse.status).isEqualTo(400)
        assertThat(errorResponse.userMessage).isEqualTo("JSON parse error: Cannot deserialize value of type `java.net.URL` from String \"not-a-valid-url\": not a valid textual representation, problem: no protocol: not-a-valid-url")
      }

      it("should return 400 given blank request ID") {
        // Given
        val requestBody =
          mapOf(
            "status" to "ALL_RELEVANT_ASSESSMENTS_COMPLETE",
            "statusChangeDate" to "2025-04-22",
            "detailUrl" to "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/A1234AB",
            "requestId" to "",
          )

        // When
        val response = mockMvc.performAuthorisedPost(apiPath(), requestBody).response

        // Then
        response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        val errorResponse = response.contentAsJson<ValidationErrorResponse>()
        assertThat(errorResponse.status).isEqualTo(400)
        assertThat(errorResponse.validationErrors).isEqualTo(listOf("A requestId must be provided"))
      }
    }
  })
