package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.kotest.assertions.json.shouldBeValidJson
import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.objectMapper
import java.time.LocalDateTime

class UpdateVisitRequestTest :
  DescribeSpec(
    {
      it("correctly creates visit queue event") {
        val consumerName = "client-name"
        val timestamp = "2020-12-04T10:42:43"
        val visitReference = "v9-d7-ed-7u"
        val updateVisitRequest =
          UpdateVisitRequest(
            visitRoom = "A1",
            visitType = VisitType.SOCIAL,
            visitRestriction = VisitRestriction.OPEN,
            startTimestamp = LocalDateTime.parse(timestamp),
            endTimestamp = LocalDateTime.parse(timestamp),
            visitNotes = listOf(VisitNotes(type = "VISITOR_CONCERN", text = "Visitor is concerned their mother in law is coming!")),
            visitContact = VisitContact(name = "John Smith", telephone = "0987654321", email = "john.smith@example.com"),
            visitors = setOf(Visitor(nomisPersonId = 3L, visitContact = true)),
            visitorSupport = VisitorSupport(description = "Visually impaired assistance"),
          )

        val hmppsMessage = updateVisitRequest.toHmppsMessage(consumerName, visitReference)
        hmppsMessage.eventType.shouldBe(HmppsMessageEventType.VISIT_UPDATED)
        hmppsMessage.who.shouldBe(consumerName)

        val hmppsMessageString = objectMapper.writeValueAsString(hmppsMessage)
        hmppsMessageString.shouldBeValidJson()
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.visitReference", visitReference)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.visitRoom", updateVisitRequest.visitRoom)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.visitType", updateVisitRequest.visitType.name)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.visitRestriction", updateVisitRequest.visitRestriction.name)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.startTimestamp", timestamp)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.endTimestamp", timestamp)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.visitNotes[0].type", updateVisitRequest.visitNotes.first().type)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.visitContact.name", updateVisitRequest.visitContact.name)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.visitors.[0].nomisPersonId", updateVisitRequest.visitors!!.first().nomisPersonId)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.visitorSupport.description", updateVisitRequest.visitorSupport!!.description)
      }
    },
  )
