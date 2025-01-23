package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Transaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionCreateResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Transactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Type
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetTransactionForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetTransactionsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.PostTransactionForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDate

@WebMvcTest(controllers = [TransactionsController::class])
@ActiveProfiles("test")
class TransactionsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getTransactionsForPersonService: GetTransactionsForPersonService,
  @MockitoBean val getTransactionForPersonService: GetTransactionForPersonService,
  @MockitoBean val postTransactionForPersonService: PostTransactionForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "200313116M"
      val prisonId = "ABC"
      val accountCode = "spends"
      val clientUniqueRef = "ABC123456X"
      val basePath = "/v1/prison/$prisonId/prisoners/$hmppsId"
      val transactionsPath = "$basePath/accounts/$accountCode/transactions"
      val transactionPath = "$basePath/transactions/$clientUniqueRef"
      val postTransactionPath = "$basePath/transactions"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      val type = "CANT"
      val description = "Canteen Purchase of £16.34"
      val amount = 1634
      val clientTransactionId = "CL123212"
      val postClientUniqueRef = "CLIENT121131-0_11"
      var exampleTransaction = TransactionRequest(type, description, amount, clientTransactionId, postClientUniqueRef)

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

      val transaction =
        Transaction(
          id = "123",
          type = Type(code = "spends", desc = "Spends"),
          amount = 100,
          date = "2016-10-21",
          description = "Spends desc",
        )

      val transactionCreateResponse = TransactionCreateResponse(transactionId = "6179604-1")

      it("calls the transactions service with expected parameters when supplied a date range") {
        val dateParams = "?from_date=2025-01-01&to_date=2025-01-01"
        mockMvc.performAuthorised(transactionsPath + dateParams)

        verify(getTransactionsForPersonService, VerificationModeFactory.times(1)).execute(hmppsId, prisonId, accountCode, "2025-01-01", "2025-01-01", null)
      }

      it("returns a prisoners transactions according to supplied code") {
        whenever(getTransactionsForPersonService.execute(hmppsId, prisonId, accountCode, "2025-01-01", "2025-01-01", null)).thenReturn(Response(transactions))

        val dateParams = "?from_date=2025-01-01&to_date=2025-01-01"
        val result = mockMvc.performAuthorised(transactionsPath + dateParams)

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

      it("returns a 404 NOT FOUND status code when could not find any transactions") {
        whenever(getTransactionsForPersonService.execute(hmppsId, prisonId, accountCode, "2025-01-01", "2025-01-01", null)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.NOMIS,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )
        val dateParams = "?from_date=2025-01-01&to_date=2025-01-01"
        val result = mockMvc.performAuthorised(transactionsPath + dateParams)

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("returns a 400 BAD REQUEST status code when there is an invalid HMPPS ID or incorrect prison") {
        whenever(getTransactionsForPersonService.execute(hmppsId, prisonId, accountCode, "2025-01-01", "2025-01-01", null)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.NOMIS,
                  type = UpstreamApiError.Type.BAD_REQUEST,
                ),
              ),
          ),
        )
        val dateParams = "?from_date=2025-01-01&to_date=2025-01-01"
        val result = mockMvc.performAuthorised(transactionsPath + dateParams)

        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      // get Transaction
      it("returns a prisoner's transaction according to clientUniqueRef") {
        whenever(getTransactionForPersonService.execute(hmppsId, prisonId, clientUniqueRef, null)).thenReturn(Response(transaction))

        val result = mockMvc.performAuthorised(transactionPath)

        result.response.contentAsString.shouldContain(
          """
          {
            "id": "123",
            "type": {
              "code": "spends",
              "desc": "Spends"
            },
            "description": "Spends desc",
            "amount": 100,
            "date": "2016-10-21"
          }
          """.removeWhitespaceAndNewlines(),
        )
      }

      it("returns a 404 NOT FOUND status code when could not find the transaction") {
        whenever(getTransactionForPersonService.execute(hmppsId, prisonId, clientUniqueRef, null)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.NOMIS,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )
        val result = mockMvc.performAuthorised(transactionPath)

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("returns a 400 BAD REQUEST status code when there is an invalid HMPPS ID or incorrect prison, to get a singular transaction") {
        whenever(getTransactionForPersonService.execute(hmppsId, prisonId, clientUniqueRef, null)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.NOMIS,
                  type = UpstreamApiError.Type.BAD_REQUEST,
                ),
              ),
          ),
        )
        val result = mockMvc.performAuthorised(transactionPath)

        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      // Post transaction
      it("returns a response with a transaction ID") {
        whenever(postTransactionForPersonService.execute(prisonId, hmppsId, exampleTransaction, null)).thenReturn(Response(transactionCreateResponse))

        val result = mockMvc.performAuthorisedPost(postTransactionPath, exampleTransaction)

        result.response.contentAsString.shouldContain(
          """
          {
            "data": {
               "transactionId": "6179604-1"
            }
          }
          """.removeWhitespaceAndNewlines(),
        )
      }

      it("returns a 200 status code when successful") {
        whenever(postTransactionForPersonService.execute(prisonId, hmppsId, exampleTransaction, null)).thenReturn(Response(transactionCreateResponse))

        val result = mockMvc.performAuthorisedPost(postTransactionPath, exampleTransaction)

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      // prison filter rejection test
      it("returns 400 if prison filter is not matched") {
        whenever(postTransactionForPersonService.execute(prisonId, hmppsId, exampleTransaction, ConsumerFilters(prisons = listOf("XYZ")))).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.NOMIS,
                  type = UpstreamApiError.Type.BAD_REQUEST,
                ),
              ),
          ),
        )

        val result = mockMvc.performAuthorisedPostWithCN(postTransactionPath, "limited-prisons", exampleTransaction)

        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("calls the API with the correct filters") {
        whenever(postTransactionForPersonService.execute(prisonId, hmppsId, exampleTransaction, ConsumerFilters(prisons = listOf("XYZ")))).thenReturn(Response(transactionCreateResponse))

        val result = mockMvc.performAuthorisedPostWithCN(postTransactionPath, "limited-prisons", exampleTransaction)

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      // bad request
      it("returns a 400 BAD REQUEST status code when there is an invalid HMPPS ID or incorrect prison, or an invalid request body") {
        whenever(postTransactionForPersonService.execute(prisonId, hmppsId, exampleTransaction, null)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.NOMIS,
                  type = UpstreamApiError.Type.BAD_REQUEST,
                ),
              ),
          ),
        )

        exampleTransaction.type = ""

        val result = mockMvc.performAuthorisedPost(postTransactionPath, exampleTransaction)

        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }
    },
  )
