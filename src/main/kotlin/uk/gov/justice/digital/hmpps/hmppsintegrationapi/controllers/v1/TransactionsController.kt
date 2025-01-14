package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Transactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetTransactionsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDate

@RestController
@RequestMapping("/v1/prison/{prisonId}/prisoners/{hmppsId}/transactions/{accountCode}")
class TransactionsController(
  @Autowired val auditService: AuditService,
  @Autowired val getTransactionsForPersonService: GetTransactionsForPersonService,
) {
  @Operation(
    summary = "Returns all transactions for a prisoner that they have at a prison.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a prisoner's transactions."),
      ApiResponse(
        responseCode = "400",
        description = "The HMPPS ID provided has an invalid format or the prisoner does hot have transactions at the specified prison.",
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
  @GetMapping()
  fun getTransactionsByAccountCode(
    @PathVariable hmppsId: String,
    @PathVariable prisonId: String,
    @PathVariable accountCode: String,
    @RequestAttribute filters: ConsumerFilters?,
    @Parameter(description = "Start date for transactions (defaults to today if not supplied)") @RequestParam(required = false, name = "from_date") fromDate: String?,
    @Parameter(description = "To date for transactions (defaults to today if not supplied)") @RequestParam(required = false, name = "to_date") toDate: String?,
  ): DataResponse<Transactions?> {
    var startDate = LocalDate.now().toString()
    var endDate = LocalDate.now().toString()

    if (fromDate == null && toDate != null || toDate == null && fromDate != null) {
      throw ValidationException("Both fromDate and toDate must be supplied if one is populated")
    }
    // catch parse failures
    if (fromDate != null && toDate != null) {
      startDate = fromDate
      endDate = toDate
    }

    if (accountCode !in listOf("spends", "savings", "cash")) {
      throw ValidationException("Account code must either be 'spends', 'savings', or 'cash'")
    }

    val response = getTransactionsForPersonService.execute(hmppsId, prisonId, accountCode, startDate, endDate, filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find transactions with id: $hmppsId")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Either invalid HMPPS ID: $hmppsId at or incorrect prison: $prisonId")
    }

    auditService.createEvent("GET_TRANSACTIONS_FOR_PERSON", mapOf("hmppsId" to hmppsId, "prisonId" to prisonId, "fromDate" to fromDate, "toDate" to toDate))
    return DataResponse(response.data)
  }
}
