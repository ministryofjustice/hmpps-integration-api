package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.kotest.assertions.json.shouldBeValidJson
import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.objectMapper

class CancelVisitRequestTest :
  DescribeSpec(
    {
      it("correctly creates cancel visit queue event") {
        val consumerName = "client-name"
        val timestamp = "2020-12-04T10:42:43"
        val cancelVisitRequest =
          CancelVisitRequest(
            cancelOutcome =
              CancelOutcome(
                outcomeStatus = OutcomeStatus.VISIT_ORDER_CANCELLED,
                text = "visit order cancelled",
              ),
            actionedBy = "test-consumer",
          )

        val hmppsMessage = cancelVisitRequest.toHmppsMessage(consumerName)
        hmppsMessage.eventType.shouldBe(HmppsMessageEventType.VISIT_CANCELLED)
        hmppsMessage.who.shouldBe(consumerName)

        val hmppsMessageString = objectMapper.writeValueAsString(hmppsMessage)
        hmppsMessageString.shouldBeValidJson()
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.cancelOutcome.text", cancelVisitRequest.cancelOutcome.text)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.cancelOutcome.outcomeStatus", cancelVisitRequest.cancelOutcome.outcomeStatus.toString())
      }
    },
  )
