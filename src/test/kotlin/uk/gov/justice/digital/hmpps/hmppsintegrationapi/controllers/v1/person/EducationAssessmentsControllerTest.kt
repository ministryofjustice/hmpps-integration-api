package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.education.EducationAssessmentSummaryResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationAssessmentStatusChangeRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.EducationAssessmentService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URI
import java.time.LocalDate

@WebMvcTest(controllers = [EducationAssessmentsController::class])
@ActiveProfiles("test")
class EducationAssessmentsControllerTest(
  private val springMockMvc: MockMvc,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val educationAssessmentService: EducationAssessmentService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      val validHmppsId = "A1234BC"
      val notFoundHmppsId = "B1234BC"
      val invalidHmppsId = "C1234BC"

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

        it("should return 404 Not Found if ENTITY_NOT_FOUND error occurs") {
          // Given
          val requestBody =
            mapOf(
              "status" to "ALL_RELEVANT_ASSESSMENTS_COMPLETE",
              "statusChangeDate" to "2025-04-22",
              "detailUrl" to "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/A1234AB",
              "requestId" to "0650ba37-a977-4fbe-9000-4715aaecadba",
            )

          val requestBodyAsRequest =
            EducationAssessmentStatusChangeRequest(
              status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
              statusChangeDate = LocalDate.of(2025, 4, 22),
              detailUrl = URI.create("https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/A1234AB").toURL(),
              requestId = "0650ba37-a977-4fbe-9000-4715aaecadba",
            )
          whenever(educationAssessmentService.sendEducationAssessmentEvent(notFoundHmppsId, requestBodyAsRequest)).thenThrow(EntityNotFoundException("Could not find person with id: $notFoundHmppsId"))
          val response = mockMvc.performAuthorisedPost("/v1/persons/$notFoundHmppsId/education/assessments/status", requestBody).response

          // Then
          response.status.shouldBe(HttpStatus.NOT_FOUND.value())
          val errorResponse = response.contentAsJson<ErrorResponse>()
          response.status.shouldBe(HttpStatus.NOT_FOUND.value())
          assertThat(errorResponse.status).isEqualTo(404)
          assertThat(errorResponse.userMessage).isEqualTo("Could not find person with id: B1234BC")
        }

        it("should throw ValidationException if an invalid hmppsId is provided") {
          // Given
          val requestBody =
            mapOf(
              "status" to "ALL_RELEVANT_ASSESSMENTS_COMPLETE",
              "statusChangeDate" to "2025-04-22",
              "detailUrl" to "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/A1234AB",
              "requestId" to "0650ba37-a977-4fbe-9000-4715aaecadba",
            )
          val requestBodyAsRequest =
            EducationAssessmentStatusChangeRequest(
              status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
              statusChangeDate = LocalDate.of(2025, 4, 22),
              detailUrl = URI.create("https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/A1234AB").toURL(),
              requestId = "0650ba37-a977-4fbe-9000-4715aaecadba",
            )
          whenever(educationAssessmentService.sendEducationAssessmentEvent(invalidHmppsId, requestBodyAsRequest)).thenThrow(ValidationException("Invalid HMPPS ID: $invalidHmppsId"))
          val response = mockMvc.performAuthorisedPost("/v1/persons/$invalidHmppsId/education/assessments/status", requestBody).response

          // Then
          val errorResponse = response.contentAsJson<ErrorResponse>()
          response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
          assertThat(errorResponse.status).isEqualTo(400)
          assertThat(errorResponse.userMessage).isEqualTo("Invalid HMPPS ID: C1234BC")
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

      describe("Get a persons education assessment summary") {
        beforeEach {
          Mockito.reset(featureFlagConfig, educationAssessmentService, auditService)
        }

        val path = "/v1/persons/$validHmppsId/education/assessments"

        it("should audit the request") {
          val response = mockMvc.performAuthorised(path).response

          verify(auditService, times(1)).createEvent("GET EDUCATION ASSESSMENT SUMMARY EVENT", mapOf("hmppsId" to validHmppsId))
        }

        describe("if the eswe curious feature flag is off") {

          it("should return 503 service unavailable") {
            whenever(featureFlagConfig.require(FeatureFlagConfig.USE_ESWE_CURIOUS_ENDPOINTS)).thenThrow(FeatureNotEnabledException(FeatureFlagConfig.USE_ESWE_CURIOUS_ENDPOINTS))

            val response = mockMvc.performAuthorised(path).response

            response.status.shouldBe(HttpStatus.SERVICE_UNAVAILABLE.value())

            val errorResponse = response.contentAsJson<ErrorResponse>()
            assertThat(errorResponse.status).isEqualTo(503)
            assertThat(errorResponse.userMessage).isEqualTo("${FeatureFlagConfig.USE_ESWE_CURIOUS_ENDPOINTS} not enabled")
          }
        }

        describe("if the education endpoint feature flag is on") {

          it("should return 200 given a valid request and response") {
            whenever(educationAssessmentService.getEducationAssessmentStatus(validHmppsId)).thenReturn(Response(data = EducationAssessmentSummaryResponse(true)))

            val response = mockMvc.performAuthorised(path).response

            response.status.shouldBe(HttpStatus.OK.value())
            response.contentAsJson<Response<EducationAssessmentSummaryResponse>>().shouldBe(Response(EducationAssessmentSummaryResponse(true)))
          }

          it("should return a 400 for a validation exception") {
            whenever(educationAssessmentService.getEducationAssessmentStatus(validHmppsId)).thenThrow(ValidationException("Invalid HMPPS ID: $validHmppsId"))

            val response = mockMvc.performAuthorised(path).response

            response.status.shouldBe(HttpStatus.BAD_REQUEST.value())

            val errorResponse = response.contentAsJson<ErrorResponse>()
            assertThat(errorResponse.status).isEqualTo(400)
            assertThat(errorResponse.userMessage).isEqualTo("Invalid HMPPS ID: $validHmppsId")
          }

          it("should return a 403 if the request is not authorised") {
            val response = mockMvc.performUnAuthorised(path).response

            response.status.shouldBe(HttpStatus.FORBIDDEN.value())
          }

          it("should return a 404 for a entity not found exception") {
            whenever(educationAssessmentService.getEducationAssessmentStatus(validHmppsId)).thenThrow(EntityNotFoundException("Could not find person with id: $validHmppsId"))

            val response = mockMvc.performAuthorised(path).response

            response.status.shouldBe(HttpStatus.NOT_FOUND.value())

            val errorResponse = response.contentAsJson<ErrorResponse>()
            assertThat(errorResponse.status).isEqualTo(404)
            assertThat(errorResponse.userMessage).isEqualTo("Could not find person with id: $validHmppsId")
          }

          it("should return a 500 for any WebClientResponse exceptions") {
            val exception = WebClientResponseException(403, "Forbidden", null, null, null)
            whenever(educationAssessmentService.getEducationAssessmentStatus(validHmppsId)).thenThrow(exception)

            val response = mockMvc.performAuthorised(path).response

            response.status.shouldBe(HttpStatus.INTERNAL_SERVER_ERROR.value())

            val errorResponse = response.contentAsJson<ErrorResponse>()
            assertThat(errorResponse.status).isEqualTo(500)
          }
        }
      }
    },
  )
