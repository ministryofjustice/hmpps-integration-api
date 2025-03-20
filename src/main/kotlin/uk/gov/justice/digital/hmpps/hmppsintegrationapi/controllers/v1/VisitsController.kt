package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
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
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CancelVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CreateVisitRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitInformationByReferenceService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.VisitQueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/visit")
@Tag(name = "prison")
class VisitsController(
  @Autowired val auditService: AuditService,
  @Autowired val visitQueueService: VisitQueueService,
  @Autowired val getVisitInformationByReferenceService: GetVisitInformationByReferenceService,
) {
  @Operation(
    summary = "Get visit information for a visit by visit reference.",
    description = "<br><br><b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found visit information for a given visit reference."),
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
  @GetMapping("/{visitReference}")
  fun getVisitInformationByReference(
    @Parameter(description = "The visit reference number relating to the visit.") @PathVariable visitReference: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<Visit?> {
    val response = getVisitInformationByReferenceService.execute(visitReference, filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find visit information for visit reference: $visitReference")
    }
    if (response.hasError(UpstreamApiError.Type.INTERNAL_SERVER_ERROR)) {
      throw Exception("Error returning from internal server error")
    }
    auditService.createEvent("GET_VISIT_INFORMATION_BY_REFERENCE", mapOf("visitReference" to visitReference))
    return DataResponse(response.data)
  }

  @Operation(
    summary = "Create visit.",
    description = "<br><br><b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully wrote to visit queue."),
      ApiResponse(
        responseCode = "400",
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
  @PostMapping("")
  fun postVisit(
    @Valid @RequestBody createVisitRequest: CreateVisitRequest,
    @RequestAttribute clientName: String?,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<HmppsMessageResponse?> {
    val response = visitQueueService.sendCreateVisit(createVisitRequest, clientName.orEmpty(), filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find prisoner")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Either invalid prisoner or prison id.")
    }

    auditService.createEvent("POST_VISIT", mapOf("prisonerId" to createVisitRequest.prisonerId, "clientVisitReference" to createVisitRequest.clientVisitReference, "clientName" to clientName.orEmpty()))

    return DataResponse(response.data)
  }

  @Operation(
    summary = "Cancel visit.",
    description = "<br><br><b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully wrote to visit queue."),
      ApiResponse(
        responseCode = "400",
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
  @PostMapping("/{visitReference}/cancel")
  fun postCancelVisit(
    @Valid @RequestBody cancelVisitRequest: CancelVisitRequest,
    @RequestAttribute clientName: String?,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<HmppsMessageResponse?> {
    val response = visitQueueService.sendCancelVisit(cancelVisitRequest, clientName.orEmpty(), filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find prisoner")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Either invalid prisoner or prison id.")
    }

    auditService.createEvent("POST_CANCEL_VISIT", mapOf("prisonerId" to cancelVisitRequest.prisonerId, "clientVisitReference" to cancelVisitRequest.clientVisitReference, "clientName" to clientName.orEmpty()))

    return DataResponse(response.data)
  }
}
