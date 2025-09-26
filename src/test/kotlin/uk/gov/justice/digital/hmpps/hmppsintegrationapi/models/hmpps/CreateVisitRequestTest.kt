package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.kotest.assertions.json.shouldBeValidJson
import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.objectMapper
import java.time.LocalDateTime

class CreateVisitRequestTest :
  DescribeSpec(
    {
      it("correctly creates visit queue event") {
        val consumerName = "client-name"
        val timestamp = "2020-12-04T10:42:43"
        val createVisitRequest =
          CreateVisitRequest(
            prisonerId = "A1234AB",
            prisonId = "MDI",
            clientVisitReference = "123456",
            visitRoom = "A1",
            visitType = VisitType.SOCIAL,
            visitRestriction = VisitRestriction.OPEN,
            startTimestamp = LocalDateTime.parse(timestamp),
            endTimestamp = LocalDateTime.parse(timestamp),
            visitNotes = listOf(VisitNotes(type = "VISITOR_CONCERN", text = "Visitor is concerned their mother in law is coming!")),
            visitContact = VisitContact(name = "John Smith", telephone = "0987654321", email = "john.smith@example.com"),
            createDateTime = LocalDateTime.parse(timestamp),
            visitors = setOf(Visitor(nomisPersonId = 3L, visitContact = true)),
            visitorSupport = VisitorSupport(description = "Visually impaired assistance"),
          )

        val hmppsMessage = createVisitRequest.toHmppsMessage(consumerName)
        hmppsMessage.eventType.shouldBe(HmppsMessageEventType.VISIT_CREATED)
        hmppsMessage.who.shouldBe(consumerName)

        val hmppsMessageString = objectMapper.writeValueAsString(hmppsMessage)
        hmppsMessageString.shouldBeValidJson()
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.prisonerId", createVisitRequest.prisonerId)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.clientName", consumerName)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.startTimestamp", timestamp)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.endTimestamp", timestamp)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.visitNotes[0].type", createVisitRequest.visitNotes.first().type)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.visitContact.name", createVisitRequest.visitContact.name)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.createDateTime", timestamp)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.visitors.[0].nomisPersonId", createVisitRequest.visitors!!.first().nomisPersonId)
        hmppsMessageString.shouldContainJsonKeyValue("$.messageAttributes.visitorSupport.description", createVisitRequest.visitorSupport!!.description)
      }
    },
  )
