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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ALNStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationALNAssessmentsChangeRequest
import java.net.URI
import java.util.UUID

class EducationALNUpdateIntegrationTest : IntegrationTestWithQueueBase("educationalnevents") {
  @DisplayName("POST /v1/persons/{hmppsId}/education/aln-assessment")
  @Nested
  inner class PostEducationStatus {
    private val path = "/v1/persons/$nomsId/education/aln-assessment"

    private fun educationALNAssessmentsChangeRequest() =
      EducationALNAssessmentsChangeRequest(
        status = ALNStatus.ASSESSMENT_COMPLETED,
        requestId = UUID.randomUUID(),
        detailUrl = URI("http://example.com/learnerAssessments/v2/D123123").toURL(),
      )

    @Test
    fun `post the education ALN assessment event, get back a message response and find a message on the queue`() {
      val request = educationALNAssessmentsChangeRequest()
      val requestBody = asJsonString(request)

      postToApi(path, requestBody)
        .andExpect(status().isCreated)
        .andExpect(
          content().json(
            """
            {
              "data": {
                  "message": "Education ALN Assessment update event written to queue"
              }
            }
            """.trimIndent(),
          ),
        )

      await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 1 }

      val queueMessages = getQueueMessages()
      queueMessages.size.shouldBe(1)

      val messageJson = queueMessages[0].body()

      val snsEnvelope = jacksonObjectMapper().readTree(messageJson)
      val eventJsonString = snsEnvelope.get("Message").asText()

      eventJsonString.shouldContainJsonKeyValue("$.eventType", "prison.education-aln-assessment.updated")

      val eventJson = jacksonObjectMapper().readTree(eventJsonString)

      val personReference = eventJson.at("/personReference/identifiers/0")
      personReference.at("/type").asText().shouldBe("NOMS")
      personReference.at("/value").asText().shouldBe(nomsId)

      val additionalInfo = eventJson.at("/additionalInformation")
      additionalInfo.at("/curiousExternalReference").asText().shouldBe(request.requestId.toString())

      eventJson.at("/description").asText().shouldBe("ASSESSMENT_COMPLETED")
      eventJson.at("/detailUrl").asText().shouldBe(request.detailUrl.toString())
    }
  }
}
