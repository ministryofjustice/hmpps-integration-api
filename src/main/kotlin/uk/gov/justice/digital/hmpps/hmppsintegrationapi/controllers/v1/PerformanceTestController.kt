package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ForbiddenByUpstreamServiceException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionCreateResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type.ENTITY_NOT_FOUND
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type.FORBIDDEN
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.PerformanceTestService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.paginateWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/v1/performance-test")
@Hidden
class PerformanceTestController(
  @Autowired val performanceTestService: PerformanceTestService,
) {
  @GetMapping("/test-1/{prisonId}/{hmppsId}")
  fun getBalancesForPerson(
    @RequestAttribute filters: ConsumerFilters?,
    @PathVariable hmppsId: String,
    @PathVariable prisonId: String,
  ): DataResponse<Balances?> {
    val response = performanceTestService.execute(prisonId, hmppsId, filters = filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Not found")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException()
    }

    return DataResponse(response.data)
  }

  @GetMapping("/test-2")
  fun getPrisoners(
    @Parameter(description = "The first name of the person") @RequestParam(required = false, name = "first_name") firstName: String?,
    @Parameter(description = "The last name of the person") @RequestParam(required = false, name = "last_name") lastName: String?,
    @Parameter(description = "The date of birth of the person") @RequestParam(required = false, name = "date_of_birth") dateOfBirth: String?,
    @Parameter(description = "Whether to return results that match the search criteria within the aliases of a person.") @RequestParam(required = false, defaultValue = "false", name = "search_within_aliases") searchWithinAliases: Boolean,
    @Parameter(description = "The page number (starting from 1)", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @Parameter(description = "The maximum number of results for a page", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
    @RequestAttribute filters: ConsumerFilters?,
  ): PaginatedResponse<PersonInPrison?> {
    if (firstName == null && lastName == null && dateOfBirth == null) {
      throw ValidationException("No query parameters specified.")
    }

    if (dateOfBirth != null && !isValidISODateFormat(dateOfBirth)) {
      throw ValidationException("Invalid date format. Please use yyyy-MM-dd.")
    }

    val response = performanceTestService.search(firstName, lastName, dateOfBirth, searchWithinAliases, filters)

    if (response.hasErrorCausedBy(FORBIDDEN, causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH)) {
      throw ForbiddenByUpstreamServiceException("Consumer configured with no access to any prisons: ${filters?.prisons}")
    }

    if (response.hasErrorCausedBy(ENTITY_NOT_FOUND, causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH)) {
      throw EntityNotFoundException("Could not find persons with supplied query parameters: $firstName, $lastName, $dateOfBirth")
    }

    return response.data.paginateWith(page, perPage)
  }

  @PostMapping("/test-3")
  fun postTransactions(
    @Parameter(description = "The ID of the prison that holds the account") @PathVariable prisonId: String,
    @Parameter(description = "The HMPPS ID of the person") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
    @RequestBody transactionRequest: TransactionRequest,
  ): DataResponse<TransactionCreateResponse?> {
    val response = performanceTestService.post(prisonId, hmppsId, transactionRequest, filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Either invalid HMPPS ID: $hmppsId or incorrect prison: $prisonId or invalid request body: ${transactionRequest.toApiConformingMap()}")
    }

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException(" ${response.errors[0].description}")
    }

    if (response.hasError(UpstreamApiError.Type.CONFLICT)) {
      throw ConflictFoundException("The transaction ${transactionRequest.clientTransactionId} has not been recorded as it is a duplicate.")
    }

    return DataResponse(response.data)
  }

  private fun isValidISODateFormat(dateString: String): Boolean =
    try {
      LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE)
      true
    } catch (e: Exception) {
      false
    }
}
