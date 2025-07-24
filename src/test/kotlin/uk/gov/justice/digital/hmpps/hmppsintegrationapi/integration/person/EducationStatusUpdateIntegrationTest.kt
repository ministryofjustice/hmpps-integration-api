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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationStatusChangeRequest
import java.net.URI
import java.util.UUID

class EducationStatusIntegrationTest : IntegrationTestWithQueueBase("educationstatusevents") {

  @DisplayName("POST /v1/persons/{hmppsId}/education/status")
  @Nested
  inner class PostEducationStatus {
    private val path = "/v1/persons/$nomsId/education/status"

    private fun educationStatusChangeRequest() =
      EducationStatusChangeRequest(
        status = EducationStatus.EDUCATION_STARTED,
        requestId = UUID.randomUUID(),
        detailUrl = URI("http://example.com/education/status").toURL(),
      )

    @Test
    fun `post the education status event, get back a message response and find a message on the queue`() {
      val request = educationStatusChangeRequest()
      val requestBody = asJsonString(request)

      postToApi(path, requestBody)
        .andExpect(status().isOk)
        .andExpect(
          content().json(
            """
            {
              "data": {
                  "message": "Education status update event written to queue"
              }
            }
            """.trimIndent()
          )
        )

      await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 1 }

      val queueMessages = getQueueMessages()
      queueMessages.size.shouldBe(1)

      val messageJson = queueMessages[0].body()

      messageJson.shouldContainJsonKeyValue("$.eventType", "prison.education.updated")

      val objectMapper = jacksonObjectMapper()
      val messageTree = objectMapper.readTree(messageJson)

      val personReference = messageTree.at("/personReference/identifiers/0")
      personReference.at("/type").asText().shouldBe("NOMS")
      personReference.at("/value").asText().shouldBe(nomsId)

      val additionalInfo = messageTree.at("/additionalInformation")
      additionalInfo.at("/curiousExternalReference").asText().shouldBe(request.requestId.toString())

      messageTree.at("/description").asText().shouldBe("EDUCATION_STARTED")
      messageTree.at("/detailUrl").asText().shouldBe(request.detailUrl.toString())
    }
  }
}
