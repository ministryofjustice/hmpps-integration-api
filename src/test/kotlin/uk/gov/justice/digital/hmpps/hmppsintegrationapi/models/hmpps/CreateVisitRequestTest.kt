package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import java.time.LocalDateTime

class CreateVisitRequestTest :
  DescribeSpec(
    {
      it("correctly creates visit queue event") {
        val consumerName = "client-name"
        val createVisitRequest =
          CreateVisitRequest(
            prisonerId = "A1234AB",
            prisonId = "MDI",
            clientVisitReference = "123456",
            visitRoom = "A1",
            visitType = VisitType.SOCIAL,
            visitStatus = VisitStatus.BOOKED,
            visitRestriction = VisitRestriction.OPEN,
            startTimestamp = LocalDateTime.parse("2020-12-04T10:42:43"),
            endTimestamp = LocalDateTime.parse("2020-12-04T10:42:43"),
            createDateTime = LocalDateTime.parse("2020-12-04T10:42:43"),
            visitors = setOf(Visitor(nomisPersonId = 3L, visitContact = true)),
            actionedBy = "test-consumer",
          )

        val visitQueueEvent = createVisitRequest.toVisitQueueEvent(consumerName)
        visitQueueEvent.eventType.shouldBe(VisitQueueEventType.CREATE)
        visitQueueEvent.payload.shouldBe(
          """
                {
                  "prisonerId" : "A1234AB",
                  "prisonId" : "MDI",
                  "clientVisitReference" : "123456",
                  "visitRoom" : "A1",
                  "visitType" : "SOCIAL",
                  "visitStatus" : "BOOKED",
                  "visitRestriction" : "OPEN",
                  "startTimestamp" : "2020-12-04T10:42:43",
                  "endTimestamp" : "2020-12-04T10:42:43",
                  "createDateTime" : "2020-12-04T10:42:43",
                  "visitors" : [ {
                    "nomisPersonId" : 3,
                    "visitContact" : true
                  } ],
                  "actionedBy" : "test-consumer"
                }
              """.removeWhitespaceAndNewlines(),
        )
        visitQueueEvent.who.shouldBe(consumerName)
      }
    },
  )
