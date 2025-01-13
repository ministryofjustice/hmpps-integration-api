package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetBalancesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/prison/{prisonId}/prisoners/{hmppsId}/balances")
class BalancesController(
  @Autowired val auditService: AuditService,
  @Autowired val getBalancesForPersonService: GetBalancesForPersonService,
) {
  @GetMapping()
  @Operation(
    summary = "Returns all accounts for a prisoner that they have at a prison.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a prisoner's accounts."),
      ApiResponse(
        responseCode = "400",
        description = "The HMPPS ID provided has an invalid format or the prisoner does hot have accounts at the specified prison.",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getBalancesForPerson(
    @PathVariable hmppsId: String,
    @PathVariable prisonId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<Balances?> {
    val response = getBalancesForPersonService.execute(prisonId, hmppsId, filters = filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Either invalid HMPPS ID: $hmppsId or incorrect prison: $prisonId")
    }

    auditService.createEvent("GET_BALANCES_FOR_PERSON", mapOf("hmppsId" to hmppsId, "prisonId" to prisonId))
    return DataResponse(response.data)
  }

  @GetMapping("/{accountCode}")
  @Operation(
    summary = "Returns a specific account for a prisoner that they have at a prison, based on the account code provided.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a prisoner's account."),
      ApiResponse(
        responseCode = "400",
        description = "The HMPPS ID provided has an invalid format, the account code is not one of the allowable accounts or the prisoner does hot have accounts at the specified prison.",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getBalanceForPerson(
    @PathVariable hmppsId: String,
    @PathVariable prisonId: String,
    @PathVariable accountCode: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<Balances?> {
    val response = getBalancesForPersonService.getBalance(prisonId, hmppsId, accountCode, filters = filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Either invalid HMPPS ID: $hmppsId, invalid Account Code: $accountCode or incorrect prison: $prisonId")
    }

    auditService.createEvent("GET_BALANCE_FOR_PERSON", mapOf("hmppsId" to hmppsId, "accountCode" to accountCode, "prisonId" to prisonId))
    return DataResponse(response.data)
  }
}
