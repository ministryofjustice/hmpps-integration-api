package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.listener

import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.IntegrationTestWithEventsQueueBase
import java.time.Duration

class NonAssociationEventIntegrationTest : IntegrationTestWithEventsQueueBase() {
  val prisonId = "MDI"

  val awaitTimeOut = Duration.ofSeconds(30)
  val awaitPollDelay = Duration.ofMillis(200)

  @BeforeEach
  fun setup() {
    eventNotificationRepository.deleteAll()
    Awaitility.setDefaultTimeout(awaitTimeOut)
    Awaitility.setDefaultPollDelay(awaitPollDelay)
  }

  @AfterEach
  fun cleanup() {
    Awaitility.reset()
  }

  @Test
  fun `will not process or save a any event triggered by a prisoner created event where there is no prison id in the response from prisoner search`() {
    prisonerOffenderSearchMockServer.stubForGet(
      "/prisoner/$nomsId",
      """
      {
        "prisonerNumber": "$nomsId",
        "firstName": "Jane",
        "lastName": "Smith"
      }
      """.trimIndent(),
    )
    generateRawPersonCreatedEvent().also { sendDomainSqsMessage(it) }
    Awaitility.await().until { eventNotificationRepository.findAll().isNotEmpty() }
    eventNotificationRepository.findAll().size.shouldBe(23)
    assertThat(getNumberOfMessagesCurrentlyOndomainEventsDeadLetterQueue()).isEqualTo(0)
  }

  @Test
  fun `will process or save a all events triggered by a prisoner created event where there is no prison id in the response from prisoner search`() {
    prisonerOffenderSearchMockServer.stubForGet(
      "/prisoner/$nomsId",
      """
      {
        "prisonerNumber": "$nomsId",
        "firstName": "Jane",
        "lastName": "Smith",
        "prisonId": "$prisonId"
      }
      """.trimIndent(),
    )
    generateRawPersonCreatedEvent().also { sendDomainSqsMessage(it) }
    Awaitility.await().until { eventNotificationRepository.findAll().isNotEmpty() }
    eventNotificationRepository.findAll().size.shouldBe(24)
    assertThat(getNumberOfMessagesCurrentlyOndomainEventsDeadLetterQueue()).isEqualTo(0)
  }

  fun generateRawPersonCreatedEvent() =
    """
    {
      "Type" : "Notification",
      "MessageId" : "eb4a33f3-1b4e-5646-80a9-52b00650b7ff",
      "TopicArn" : "N/A",
      "Message" : "{\"additionalInformation\":{\"nomsNumber\":\"$nomsId\"},\"occurredAt\":\"2025-09-16T09:07:56.135238735+01:00\",\"eventType\":\"prisoner-offender-search.prisoner.created\",\"version\":1,\"description\":\"A prisoner record has been created\",\"detailUrl\":\"http://localhost:8080/prisoner/A1234BD\",\"personReference\":{\"identifiers\":[{\"type\":\"NOMS\",\"value\":\"$nomsId\"}]}}",
      "Timestamp" : "2025-09-16T08:07:58.218Z",
      "SignatureVersion" : "1",
      "Signature" : "N/A",
      "SigningCertURL" : "N/A",
      "UnsubscribeURL" : "N/A",
      "MessageAttributes" : {
        "eventType" : {"Type":"String","Value":"prisoner-offender-search.prisoner.created"}
      }
    }
    """.trimIndent()
}
