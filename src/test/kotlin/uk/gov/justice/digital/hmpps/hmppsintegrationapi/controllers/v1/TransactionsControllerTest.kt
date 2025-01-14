package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Transaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Transactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Type
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetTransactionsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDate

@WebMvcTest(controllers = [TransactionsController::class])
@ActiveProfiles("test")
class TransactionsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getTransactionsForPersonService: GetTransactionsForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "200313116M"
      val prisonId = "ABC"
      val accountCode = "spends"
      val basePath = "/v1/prison/$prisonId/prisoners/$hmppsId/transactions/$accountCode"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      val transactions =
        Transactions(
          transactions =
            listOf(
              Transaction(
                id = "123",
                type = Type(code = "spends", desc = "Spends"),
                amount = 100,
                date = LocalDate.parse("2025-01-01").toString(),
                description = "Spends desc",
              ),
            ),
        )

      it("calls the service with expected parameters when supplied a date range") {
        val dateParams = "?from_date=2025-01-01&to_date=2025-01-01"
        mockMvc.performAuthorised(basePath + dateParams)

        verify(getTransactionsForPersonService, VerificationModeFactory.times(1)).execute(hmppsId, prisonId, accountCode, "2025-01-01", "2025-01-01", null)
      }

      it("returns a prisoners transactions according to supplied code") {
        whenever(getTransactionsForPersonService.execute(hmppsId, prisonId, accountCode, "2025-01-01", "2025-01-01", null)).thenReturn(Response(transactions))

        val dateParams = "?from_date=2025-01-01&to_date=2025-01-01"
        val result = mockMvc.performAuthorised(basePath + dateParams)

        result.response.contentAsString.shouldContain(
          """
            {
            "data": {
            "transactions": [
              {
                "id": "123",
                "type": {
                  "code": "spends",
                  "desc": "Spends"
                },
                "description": "Spends desc",
                "amount": 100,
                "date": "2025-01-01"
              }
            ]
          }
          }
          """.removeWhitespaceAndNewlines(),
        )
      }
    },
  )
