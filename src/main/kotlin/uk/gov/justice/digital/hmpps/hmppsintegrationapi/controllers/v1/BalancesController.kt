package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.InternalServerErrorException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Balances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetBalancesForPersonService

@RestController
@RequestMapping("/v1/prison/{prisonId}/prisoners/{hmppsId}/balances")
class BalancesController(@Autowired val getBalancesForPersonService: GetBalancesForPersonService,) {
  @GetMapping()
  @Operation(
    summary = "Returns a all accounts for a prisoner that they have at a prison.",
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
  fun getBalancesForPerson(@PathVariable hmppsId: String, @PathVariable prisonId: String): DataResponse<Balances?> {
    val response = getBalancesForPersonService.execute(prisonId, hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw BadRequestException("Either invalid HMPPS ID: $hmppsId at or incorrect prison: $prisonId")
    }

    if (response.hasError(UpstreamApiError.Type.INTERNAL_SERVER_ERROR)) {
      throw InternalServerErrorException("Error occurred while trying to get accounts for person with id: $hmppsId")
    }
    return DataResponse(response.data)
  }
}
