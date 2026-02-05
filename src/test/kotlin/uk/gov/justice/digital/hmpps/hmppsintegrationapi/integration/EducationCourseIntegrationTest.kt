package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import io.kotest.matchers.shouldBe
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class EducationCourseIntegrationTest : IntegrationTestWithQueueBase("educationcoursecompletionevents") {
  @Test
  fun `post education course completion`() {
    val requestBody =
      """
      {
        "courseCompletion": {
          "externalReference": "CC123",
          "person": {
            "firstName": "John",
            "lastName": "Doe",
            "dateOfBirth": "1990-01-01",
            "region": "London",
            "email": "john.doe@example.com"
          },
          "course": {
            "courseName": "Test Course",
            "courseType": "Test course type",
            "provider": "Moodle",
            "completionDate": "2024-01-15",
            "status": "Completed",
            "totalTimeMinutes": 150,
            "attempts": 1,
            "expectedTimeMinutes": 120
          }
        }
      }
      """.trimIndent()

    postToApi("/v1/education/course-completion", requestBody)
      .andExpect(status().isAccepted)

    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 1 }

    val queueMessages = getQueueMessages()
    queueMessages.size shouldBe 1
  }
}
