package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AccountBalance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetBalancesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

@WebMvcTest(controllers = [BalancesController::class])
@ActiveProfiles("test")
class BalancesControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val getBalancesForPersonService: GetBalancesForPersonService,
) : DescribeSpec({
    val hmppsId = "200313116M"
    val prisonId = "ABC"

    val basePath = "/v1/prison/$prisonId/prisoners/$hmppsId/balances"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)
    val objectMapper = ObjectMapper()
    val balance =
      Balances(
        balances =
          listOf(
            AccountBalance(accountCode = "spends", amount = 101),
            AccountBalance(accountCode = "saving", amount = 102),
            AccountBalance(accountCode = "cash", amount = 103),
          ),
      )
    it("gets the balances for a person with the matching ID") {
      mockMvc.performAuthorised(basePath)

      verify(getBalancesForPersonService, VerificationModeFactory.times(1)).execute(prisonId, hmppsId)
    }

    it("returns the correct balances data") {
      whenever(getBalancesForPersonService.execute(prisonId, hmppsId)).thenReturn(
        Response(
          data = balance,
        ),
      )
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
        """.removeWhitespaceAndNewlines(),
      )
    }
  })
