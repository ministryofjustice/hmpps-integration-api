package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetBalancesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

@WebMvcTest(controllers = [BalancesController::class])
@ActiveProfiles("test")
class BalancesControllerTests(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val getBalancesForPersonService: GetBalancesForPersonService,
) {
  val hmppsId = "200313116M"
  val prisonId = "ABC"

  val basePath = "/v1/prison/$prisonId/prisoners/$hmppsId/balances"
  val mockMvc = IntegrationAPIMockMvc(springMockMvc)
  val objectMapper = ObjectMapper()

  @Test
  fun `returns the balances data`() {
    val result = mockMvc.performAuthorised(basePath)
    result.response.contentAsString.shouldContain(
      """
          "data": {
            "balances": [
              {
                "accountCode": "spends",
                "amount": 101
              },
              {
                "accountCode": "saving",
                "amount": 102
              },
              {
                "accountCode": "cash",
                "amount": 103
              }
            ]
          }
        """.removeWhitespaceAndNewlines()
    )
  }
}
