package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitInformationByReferenceService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/visit")
@Tag(name = "prison")
class VisitsController(
  @Autowired val auditService: AuditService,
  @Autowired val getVisitInformationByReferenceService: GetVisitInformationByReferenceService,
) {
  @Operation(
    summary = "Get visit information for a visit by visit reference.",
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
}
