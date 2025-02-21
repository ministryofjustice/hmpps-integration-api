package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VisitsIntegrationTest : IntegrationTestBase() {
  val visitReference = "123456"

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
                  "visitors": [{ "nomisPersonId": 1234, "visitContact": true}],
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
