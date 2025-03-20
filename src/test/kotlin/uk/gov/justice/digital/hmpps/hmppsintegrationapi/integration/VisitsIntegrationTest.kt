package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CreateVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visitor
import java.time.LocalDateTime

class VisitsIntegrationTest : IntegrationTestBase() {
  @Nested
  inner class GetVisitByReference {
    private val visitReference = "123456"

    @Test
    fun `gets the visit detail`() {
      callApi("/v1/visit/$visitReference")
        .andExpect(status().isOk)
        .andExpect(
          content().json(
            """
            {
              "data": {
                  "prisonerId": "AF34567G",
                  "prisonId": "MDI",
                  "prisonName": "Moorland (HMP & YOI)",
                  "visitRoom": "Visits Main Hall",
                  "visitType": "SOCIAL",
                  "visitStatus": "BOOKED",
                  "outcomeStatus": "ADMINISTRATIVE_CANCELLATION",
                  "visitRestriction": "OPEN",
                  "startTimestamp": "2018-12-01T13:45:00",
                  "endTimestamp": "2018-12-01T13:45:00",
                  "createdTimestamp": "2018-12-01T13:45:00",
                  "modifiedTimestamp": "2018-12-01T13:45:00",
                  "firstBookedDateTime": "2018-12-01T13:45:00",
                  "visitors": [{ "contactId": 1234, "visitContact": true}],
                  "visitNotes": [{ "type": "VISITOR_CONCERN", "text": "Visitor is concerned that his mother in-law is coming!"}],
                  "visitContact": {"name": "John Smith", "telephone": "01234 567890", "email": "email@example.com"},
                  "visitorSupport": {"description": "visually impaired assistance"}
              }
            }
      """,
          ),
        )
    }

    @Test
    fun `return a 404 when prison not in filter`() {
      callApiWithCN("/v1/visit/$visitReference", limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      callApiWithCN("/v1/visit/$visitReference", noPrisonsCn)
        .andExpect(status().isNotFound)
    }
  }

  @Nested
  inner class PostVisit {
    private val clientName = "automated-test-client"
    private val timestamp = "2020-12-04T10:42:43"
    private val prisonerId = "A1234AB"

    private fun getCreateVisitRequestBody(prisonerId: String): String {
      val createVisitRequest =
        CreateVisitRequest(
          prisonerId = prisonerId,
          prisonId = "MDI",
          clientVisitReference = "123456",
          visitRoom = "A1",
          visitType = VisitType.SOCIAL,
          visitStatus = VisitStatus.BOOKED,
          visitRestriction = VisitRestriction.OPEN,
          startTimestamp = LocalDateTime.parse(timestamp),
          endTimestamp = LocalDateTime.parse(timestamp),
          createDateTime = LocalDateTime.parse(timestamp),
          visitors = setOf(Visitor(nomisPersonId = 3L, visitContact = true)),
          actionedBy = clientName,
        )
      return asJsonString(createVisitRequest)
    }

    @Test
    fun `post the visit and get back a message response`() {
      val requestBody = getCreateVisitRequestBody(prisonerId)

      postToApi("/v1/visit", requestBody)
        .andExpect(status().isOk)
        .andExpect(
          content().json(
            """
            {
              "data": {
                  "message": "Visit creation written to queue"
              }
            }
            """,
          ),
        )
    }

    @Test
    fun `return a 400 when prisoner ID not valid`() {
      val requestBody = getCreateVisitRequestBody("INVALID_PRISON_ID")

      postToApiWithCN("/v1/visit", requestBody, limitedPrisonsCn)
        .andExpect(status().isBadRequest)
    }

    @Test
    fun `return a 404 when prison not in filter`() {
      val requestBody = getCreateVisitRequestBody(prisonerId)

      postToApiWithCN("/v1/visit", requestBody, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      val requestBody = getCreateVisitRequestBody(prisonerId)

      postToApiWithCN("/v1/visit", requestBody, noPrisonsCn)
        .andExpect(status().isNotFound)
    }
  }
}
