package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class FutureVisitsIntegrationTest : IntegrationTestBase() {
  @Test
  fun `gets the future visits`() {
    callApi("$basePath/$nomsId/visit/future")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
            {
              "data": [
                {
                  "applicationReference": "dfs-wjs-eqr",
                  "reference": "v9-d7-ed-7u",
                  "prisonerId": "AF34567G",
                  "prisonId": "MDI",
                  "prisonName": "Moorland (HMP & YOI)",
                  "sessionTemplateReference": "v9d.7ed.7u",
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
              ]
            }
        """,
        ),
      )
  }

  @Test
  fun `return a 404 when prison not in filter`() {
    callApiWithCN("$basePath/$nomsId/visit/future", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 when no prisons in filter`() {
    callApiWithCN("$basePath/$nomsId/visit/future", noPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
