package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivateLocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivationReason
import java.time.LocalDate

class DeactivateLocationIntegrationTest : IntegrationTestBase() {
  private val prisonId = "MDI"
  val key = "MDI-A-1-001"
  val path = "/v1/prison/$prisonId/location/$key/deactivate"
  val deactivateLocationRequest =
    DeactivateLocationRequest(
      deactivationReason = DeactivationReason.DAMAGED,
      deactivationReasonDescription = "Scheduled maintenance",
      proposedReactivationDate = LocalDate.now(),
      planetFmReference = "23423TH/5",
    )

  @Test
  fun `return the response saying message on queue`() {
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
  }

  @Test
  fun `return a 404 when prison not in the allowed prisons`() {
    postToApiWithCN(path, asJsonString(deactivateLocationRequest), limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 no prisons in filter`() {
    postToApiWithCN(path, asJsonString(deactivateLocationRequest), noPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
