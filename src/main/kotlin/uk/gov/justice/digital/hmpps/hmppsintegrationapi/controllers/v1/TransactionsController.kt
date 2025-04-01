package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ConflictFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Transaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionCreateResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionTransferCreateResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionTransferRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetTransactionForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetTransactionsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.PostTransactionForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.PostTransactionTransferForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.paginateWith
import java.time.LocalDate

@RestController
@RequestMapping("/v1/prison/{prisonId}/prisoners/{hmppsId}")
@Tag(name = "prison")
class TransactionsController(
  @Autowired val auditService: AuditService,
  @Autowired val getTransactionsForPersonService: GetTransactionsForPersonService,
  @Autowired val getTransactionForPersonService: GetTransactionForPersonService,
  @Autowired val postTransactionsForPersonService: PostTransactionForPersonService,
  @Autowired val postTransactionTransferForPersonService: PostTransactionTransferForPersonService,
) {
  @Operation(
    summary = "Returns all transactions for a prisoner associated with an account code that they have at a prison.",
    description = "The clientUniqueRef will only be present if it was specified by an API client.<br><b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a prisoner's transactions."),
      ApiResponse(
        responseCode = "400",
        description = "The request data has an invalid format or the prisoner does hot have transactions at the specified prison.",
        content = [
          Content(
            schema =
              io.swagger.v3.oas.annotations.media
                .Schema(ref = "#/components/schemas/BadRequest"),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        content = [
          Content(
            schema =
              io.swagger.v3.oas.annotations.media
                .Schema(ref = "#/components/schemas/PersonNotFound"),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "500",
        content = [
          Content(
            schema =
              io.swagger.v3.oas.annotations.media
                .Schema(ref = "#/components/schemas/InternalServerError"),
          ),
        ],
      ),
    ],
  )
  @GetMapping("/accounts/{accountCode}/transactions")
  fun getTransactionsByAccountCode(
    @Parameter(description = "The HMPPS ID of the person") @PathVariable hmppsId: String,
    @Parameter(description = "The ID of the prison that holds the account") @PathVariable prisonId: String,
    @Parameter(description = "The code of the account to be accessed, one of 'spends', 'savings' or 'cash'", example = "spends") @PathVariable accountCode: String,
    @RequestAttribute filters: ConsumerFilters?,
    @Parameter(description = "Start date for transactions (defaults to today if not supplied)") @RequestParam(required = false, name = "from_date") fromDate: String?,
    @Parameter(description = "To date for transactions (defaults to today if not supplied)") @RequestParam(required = false, name = "to_date") toDate: String?,
    @Parameter(description = "The page number (starting from 1)", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @Parameter(description = "The maximum number of results for a page", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
  ): PaginatedResponse<Transaction?> {
    var startDate = LocalDate.now().toString()
    var endDate = LocalDate.now().toString()

    if (fromDate == null && toDate != null || toDate == null && fromDate != null) {
      throw ValidationException("Both fromDate and toDate must be supplied if one is populated")
    }

    if (fromDate != null && toDate != null) {
      startDate = fromDate
      endDate = toDate
    }

    val response = getTransactionsForPersonService.execute(hmppsId, prisonId, accountCode, startDate, endDate, filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find transactions with id: $hmppsId")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Either invalid HMPPS ID: $hmppsId or incorrect prison: $prisonId")
    }
    auditService.createEvent("GET_TRANSACTIONS_FOR_PERSON", mapOf("hmppsId" to hmppsId, "prisonId" to prisonId, "fromDate" to fromDate, "toDate" to toDate))

    return response.data.orEmpty().paginateWith(page, perPage)
  }

  @Operation(
    summary = "Returns details for a transaction by clientUniqueRef.",
    description = "The clientUniqueRef will not be present on the response.<br><b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a transaction."),
      ApiResponse(
        responseCode = "400",
        description = "The request data has an invalid format or the prisoner does not have transaction associated with the clientUniqueRef.",
        content = [
          Content(
            schema =
              io.swagger.v3.oas.annotations.media
                .Schema(ref = "#/components/schemas/BadRequest"),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        content = [
          Content(
            schema =
              io.swagger.v3.oas.annotations.media
                .Schema(ref = "#/components/schemas/PersonNotFound"),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "500",
        content = [
          Content(
            schema =
              io.swagger.v3.oas.annotations.media
                .Schema(ref = "#/components/schemas/InternalServerError"),
          ),
        ],
      ),
    ],
  )
  @GetMapping("/transactions/{clientUniqueRef}")
  fun getTransactionsByClientUniqueRef(
    @Parameter(description = "The HMPPS ID of the person") @PathVariable prisonId: String,
    @Parameter(description = "The ID of the prison that holds the account") @PathVariable hmppsId: String,
    @Parameter(description = "The clientUniqueRef used when the transaction was created") @PathVariable clientUniqueRef: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<Transaction?> {
    val response = getTransactionForPersonService.execute(hmppsId, prisonId, clientUniqueRef, filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find transaction with id: $hmppsId")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Either invalid HMPPS ID: $hmppsId or incorrect prison: $prisonId")
    }

    auditService.createEvent("GET_TRANSACTION_FOR_PERSON", mapOf("hmppsId" to hmppsId, "prisonId" to prisonId, "clientUniqueRef" to clientUniqueRef))
    return DataResponse(response.data)
  }

  @Operation(
    summary = "Make a financial transaction.",
    description = "<a href=\"#schema-transactionrequest\">Request body</a><br><b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully created a transaction."),
      ApiResponse(
        responseCode = "400",
        description = "",
        content = [
          Content(
            schema =
              io.swagger.v3.oas.annotations.media
                .Schema(ref = "#/components/schemas/BadRequest"),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        content = [
          Content(
            schema =
              io.swagger.v3.oas.annotations.media
                .Schema(ref = "#/components/schemas/PersonNotFound"),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "409",
        content = [
          Content(
            schema =
              io.swagger.v3.oas.annotations.media
                .Schema(ref = "#/components/schemas/TransactionConflict"),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "500",
        content = [
          Content(
            schema =
              io.swagger.v3.oas.annotations.media
                .Schema(ref = "#/components/schemas/InternalServerError"),
          ),
        ],
      ),
    ],
  )
  @PostMapping("/transactions")
  fun postTransactions(
    @Parameter(description = "The ID of the prison that holds the account") @PathVariable prisonId: String,
    @Parameter(description = "The HMPPS ID of the person") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
    @Valid @RequestBody transactionRequest: TransactionRequest,
  ): DataResponse<TransactionCreateResponse?> {
    val response = postTransactionsForPersonService.execute(prisonId, hmppsId, transactionRequest, filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Either invalid HMPPS ID: $hmppsId or incorrect prison: $prisonId or invalid request body: ${transactionRequest.toApiConformingMap()}")
    }

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException(" ${response.errors[0].description}")
    }

    if (response.hasError(UpstreamApiError.Type.CONFLICT)) {
      throw ConflictFoundException("The transaction ${transactionRequest.clientTransactionId} has not been recorded as it is a duplicate.")
    }

    auditService.createEvent("CREATE_TRANSACTION", mapOf("hmppsId" to hmppsId, "prisonId" to prisonId, "transactionRequest" to transactionRequest.toApiConformingMap().toString()))
    return DataResponse(response.data)
  }

  @Operation(
    summary = "Transfer funds between the accounts of a prisoner.",
    description = "Currently only able to move from spends to savings.<br><a href=\"#schema-transactiontransferrequest\">Request body</a><br><br><b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully created a transaction transfer."),
      ApiResponse(
        responseCode = "400",
        description = "",
        content = [
          Content(
            schema =
              io.swagger.v3.oas.annotations.media
                .Schema(ref = "#/components/schemas/BadRequest"),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        content = [
          Content(
            schema =
              io.swagger.v3.oas.annotations.media
                .Schema(ref = "#/components/schemas/PersonNotFound"),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "409",
        content = [
          Content(
            schema =
              io.swagger.v3.oas.annotations.media
                .Schema(ref = "#/components/schemas/TransactionConflict"),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "500",
        content = [
          Content(
            schema =
              io.swagger.v3.oas.annotations.media
                .Schema(ref = "#/components/schemas/InternalServerError"),
          ),
        ],
      ),
    ],
  )
  @PostMapping("/transactions/transfer")
  fun postTransactionsTransfer(
    @Parameter(description = "The ID of the prison that holds the account") @PathVariable prisonId: String,
    @Parameter(description = "The HMPPS ID of the person") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
    @Valid @RequestBody transactionTransferRequest: TransactionTransferRequest,
  ): DataResponse<TransactionTransferCreateResponse?> {
    val response = postTransactionTransferForPersonService.execute(prisonId, hmppsId, transactionTransferRequest, filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Either invalid HMPPS ID: $hmppsId or incorrect prison: $prisonId or invalid request body: ${response.errors[0].description}")
    }

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException(" ${response.errors[0].description}")
    }

    if (response.hasError(UpstreamApiError.Type.CONFLICT)) {
      throw ConflictFoundException("The transaction ${transactionTransferRequest.clientTransactionId} has not been recorded as it is a duplicate.")
    }

    auditService.createEvent("CREATE_TRANSACTION_TRANSFER", mapOf("hmppsId" to hmppsId, "prisonId" to prisonId, "transactionTransferRequest" to transactionTransferRequest.toString()))
    return DataResponse(response.data)
  }
}
