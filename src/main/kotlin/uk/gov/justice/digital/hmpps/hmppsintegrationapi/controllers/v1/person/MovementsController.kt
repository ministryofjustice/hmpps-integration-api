package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.MovementDiary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetMovementsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons")
@Tags(value = [Tag(name = "Persons"), Tag(name = "Movements")])
class MovementsController(
  private val getMovementsService: GetMovementsService,
  private val auditService: AuditService,
) {
  @GetMapping("{hmppsId}/movements/scheduled")
  @Operation(
    summary = "Returns a summary of prisoner movements.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found Movement Dairy for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getMovementsSummary(
    @Parameter(description = "A HMPPS id", example = "A123123") @PathVariable hmppsId: String,
    @RequestAttribute requestContext: RequestContext,
  ): DataResponse<List<MovementDiary>> {
    val response = getMovementsService.getMovementsSummary(hmppsId, requestContext)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent("GET_MOVEMENTS_SUMMARY", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }
}

@RestController
@RequestMapping("/v1")
@Tags(Tag(name = "Persons"))
class MovementsController(
  @Autowired val getMovementService: GetMovementService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("/persons/{hmppsId}/movements/transfer-summary")
  @Operation(
    summary = "Returns prisoner movements transfer summary.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Returns prisoner movements with the provided HMPPS Id"),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonMovementSummary(
    @Parameter(description = "A HMPPS id", example = "A1234AA") @PathVariable hmppsId: String,
    @RequestAttribute requestContext: RequestContext?,
  ): DataResponse<PrisonerMovementsResponse> {
    val response = getMovementService.getMovement(hmppsId, requestContext)
    ensureResponse(hmppsId, response)

    auditService.createEvent("GET_PERSON_MOVEMENT_SUMMARY", mapOf("hmppsId" to hmppsId))

    return DataResponse(response.data!!)
  }

  private fun ensureResponse(
    hmppsId: String,
    response: Response<PrisonerMovementsResponse?>,
  ) {
    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
  }
}
