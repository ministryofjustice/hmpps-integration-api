package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.matchers.shouldBe
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestWithQueueBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationAssessmentStatusChangeRequest
import java.net.URI
import java.time.LocalDate
import java.util.UUID

class EducationAssessmentsIntegrationTest : IntegrationTestWithQueueBase("assessmentevents") {
  @DisplayName("POST /v1/persons/{hmppsId}/education/assessments/status")
  @Nested
  inner class PostVisit {
    private val path = "/v1/persons/A1234AB/education/assessments/status"

    private fun educationAssessmentStatusChangeRequest() =
      EducationAssessmentStatusChangeRequest(
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        requestId = UUID.randomUUID().toString(),
        statusChangeDate = LocalDate.now(),
        detailUrl = URI("http://deatail.url").toURL(),
      )

    @Test
    fun `post the assessment event, get back a message response and find a message on the queue`() {
      val educationAssessmentStatusChangeRequest = educationAssessmentStatusChangeRequest()
      val requestBody = asJsonString(educationAssessmentStatusChangeRequest)

      postToApi(path, requestBody)
        .andExpect(status().isOk)
        .andExpect(
          content().json(
            """
            {
              "data": {
                  "message": "Education assessment event written to queue"
              }
            }
            """,
          ),
        )

      await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 1 }

      val queueMessages = getQueueMessages()
      queueMessages.size.shouldBe(1)

      val messageJson = queueMessages[0].body()

      val expectedMessageAttributes =
        mapOf(
          "prisonNumber" to "A1234AB",
          "status" to educationAssessmentStatusChangeRequest.status,
          "statusChangeDate" to educationAssessmentStatusChangeRequest.statusChangeDate.toString(),
          "detailUrl" to educationAssessmentStatusChangeRequest.detailUrl,
          "requestId" to educationAssessmentStatusChangeRequest.requestId,
        )

      messageJson.shouldContainJsonKeyValue("$.eventType", "EducationAssessmentEventCreated")

      val objectMapper = jacksonObjectMapper()
      val messageAttributes = objectMapper.readTree(messageJson).at("/messageAttributes")
      val expectedMessageAttributesAsJson = objectMapper.readTree(objectMapper.writeValueAsString(expectedMessageAttributes))
      messageAttributes.shouldBe(expectedMessageAttributesAsJson)
    }
  }
}
