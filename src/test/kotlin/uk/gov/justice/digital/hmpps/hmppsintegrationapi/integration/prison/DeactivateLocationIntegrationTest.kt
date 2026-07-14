package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import com.atlassian.oai.validator.wiremock.OpenApiValidationListener
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.matchers.shouldBe
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestWithQueueBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivateLocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivationReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchQuery
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchRequest
import java.time.LocalDate

class DeactivateLocationIntegrationTest : IntegrationTestWithQueueBase("locations") {
  private val prisonId = "MDI"
  private val key = "MDI-A-1-001"
  private val path = "/v1/prison/$prisonId/location/$key/deactivate"
  private val deactivateLocationRequest =
    DeactivateLocationRequest(
      deactivationReason = DeactivationReason.DAMAGED,
      deactivationReasonDescription = "Scheduled maintenance",
      proposedReactivationDate = LocalDate.now(),
    )
  private val cellRequest =
    POSAttributeSearchRequest(
      joinType = "AND",
      queries =
        listOf(
          POSAttributeSearchQuery(
            joinType = "AND",
            matchers =
              listOf(
                POSAttributeSearchMatcher(
                  type = "String",
                  attribute = "prisonId",
                  condition = "IS",
                  searchTerm = "$prisonId",
                ),
                POSAttributeSearchMatcher(
                  type = "String",
                  attribute = "cellLocation",
                  condition = "IS",
                  searchTerm = "A-1-001",
                ),
              ),
          ),
        ),
    )

  private val invalidCellRequest =
    POSAttributeSearchRequest(
      joinType = "AND",
      queries =
        listOf(
          POSAttributeSearchQuery(
            joinType = "ANDOR",
            matchers =
              listOf(
                POSAttributeSearchMatcher(
                  type = "String",
                  attribute = "prisonId",
                  condition = "IS",
                  searchTerm = "$prisonId",
                ),
                POSAttributeSearchMatcher(
                  type = "String",
                  attribute = "cellLocation",
                  condition = "IS",
                  searchTerm = "A-1-001",
                ),
              ),
          ),
        ),
    )

  @AfterEach
  fun resetValidators() {
    prisonerOffenderSearchMockServer.resetValidator()
  }

  @Test
  fun `return the response saying message on queue`() {
    prisonerOffenderSearchMockServer.stubForPost(
      "/attribute-search",
      jacksonObjectMapper().writeValueAsString(cellRequest),
      """
      {
        "content": [],
        "pageable": {
          "sort": {
            "empty": true,
            "unsorted": true,
            "sorted": false
          },
          "offset": 0,
          "pageSize": 10,
          "pageNumber": 0,
          "paged": true,
          "unpaged": false
        },
        "totalPages": 1,
        "last": false,
        "totalElements": 1,
        "size": 10,
        "number": 0,
        "sort": {
          "empty": true,
          "unsorted": true,
          "sorted": false
        },
        "first": true,
        "numberOfElements": 1,
        "empty": false
      }
      """.trimIndent(),
    )

    postToApi(path, asJsonString(deactivateLocationRequest))
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
          {
            "data": {
              "message": "Deactivate location written to queue"
            }
          }
          """.trimIndent(),
        ),
      )
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 1 }

    val queueMessages = getQueueMessages()
    queueMessages.size.shouldBe(1)

    val messageJson = queueMessages[0].body()
    val expectedMessage = deactivateLocationRequest.toHmppsMessage(locationId = "2475f250-434a-4257-afe7-b911f1773a4d", defaultCn)
    messageJson.shouldContainJsonKeyValue("$.eventType", expectedMessage.eventType.eventTypeCode)
    messageJson.shouldContainJsonKeyValue("$.who", defaultCn)
    val objectMapper = jacksonObjectMapper()
    val messageAttributes = objectMapper.readTree(messageJson).at("/messageAttributes")
    val expectedMessageAttributes = objectMapper.readTree(objectMapper.writeValueAsString(expectedMessage.messageAttributes))
    messageAttributes.shouldBe(expectedMessageAttributes)
    prisonerOffenderSearchMockServer.assertValidationPassed()
  }

  @Test
  fun `an invalid cell request with unknown join type fails schema validation`() {
    prisonerOffenderSearchMockServer.stubForPost(
      "/attribute-search",
      jacksonObjectMapper().writeValueAsString(invalidCellRequest),
      """
      {
        "errorCode": 400,
        "status": 400,
        "userMessage": "Bad request"
      }
      """.trimIndent(),
      HttpStatus.BAD_REQUEST,
    )
    val response =
      assertThrows<WebClientResponseException> {
        prisonerOffenderSearchGateway.attributeSearch(invalidCellRequest)
      }

    val validationFailure =
      assertThrows<OpenApiValidationListener.OpenApiValidationException> {
        prisonerOffenderSearchMockServer.assertValidationPassed()
      }

    validationFailure.validationReport.hasErrors().shouldBe(true)
    validationFailure.validationReport.messages
      .first()
      .message
      .shouldBe("[Path '/queries/0/joinType'] Instance value (\"ANDOR\") not found in enum (possible values: [\"AND\",\"OR\"])")

    response.statusText.shouldBe("Bad Request")

    prisonerOffenderSearchMockServer.assertValidationFailed()
  }

  @Test
  fun `return the response saying message on queue for test event`() {
    postToApi(path, asJsonString(deactivateLocationRequest.copy(externalReference = "TestEvent")))
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
          {
            "data": {
              "message": "Deactivate location written to queue"
            }
          }
          """.trimIndent(),
        ),
      )

    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 1 }

    val queueMessages = getQueueMessages()
    queueMessages.size.shouldBe(1)

    val messageJson = queueMessages[0].body()
    val expectedMessage = deactivateLocationRequest.toTestMessage(defaultCn)
    messageJson.shouldContainJsonKeyValue("$.eventType", expectedMessage.eventType.eventTypeCode)
    messageJson.shouldContainJsonKeyValue("$.who", defaultCn)
    val objectMapper = jacksonObjectMapper()
    val messageAttributes = objectMapper.readTree(messageJson).at("/messageAttributes")
    val expectedMessageAttributes = objectMapper.readTree(objectMapper.writeValueAsString(expectedMessage.messageAttributes))
    messageAttributes.shouldBe(expectedMessageAttributes)
  }

  @Test
  fun `return a 409 when cell is not empty`() {
    prisonerOffenderSearchMockServer.stubForPost(
      "/attribute-search",
      jacksonObjectMapper().writeValueAsString(cellRequest),
      """
      {
        "content": [
          {
            "prisonId": "MDI",
            "firstName": "Rich",
            "lastName": "Roger",
            "cellLocation": "A-1-001"
          }
        ],
        "pageable": {
          "sort": {
            "empty": true,
            "unsorted": true,
            "sorted": false
          },
          "offset": 0,
          "pageSize": 10,
          "pageNumber": 0,
          "paged": true,
          "unpaged": false
        },
        "totalPages": 1,
        "last": false,
        "totalElements": 1,
        "size": 10,
        "number": 0,
        "sort": {
          "empty": true,
          "unsorted": true,
          "sorted": false
        },
        "first": true,
        "numberOfElements": 1,
        "empty": false
      }
      """.trimIndent(),
    )

    postToApi(path, asJsonString(deactivateLocationRequest))
      .andExpect(status().isConflict)

    checkQueueIsEmpty()
  }

  @Test
  fun `return a 404 when prison not in the allowed prisons`() {
    postToApiWithCN(path, asJsonString(deactivateLocationRequest), limitedPrisonsCn)
      .andExpect(status().isNotFound)

    checkQueueIsEmpty()
  }

  @Test
  fun `return a 404 no prisons in filter`() {
    postToApiWithCN(path, asJsonString(deactivateLocationRequest), noPrisonsCn)
      .andExpect(status().isNotFound)

    checkQueueIsEmpty()
  }
}
