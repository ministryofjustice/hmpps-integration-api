package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type.ENTITY_NOT_FOUND
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation.NonAssociations
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonersNonAssociationsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@Tag(name = "prison")
class OffenderRestrictionsController(
  @Autowired val auditService: AuditService,
  @Autowired val getPrisonersNonAssociationsService: GetPrisonersNonAssociationsService,
) {
  @GetMapping("/v1/prison/{prisonId}/prisoners/{hmppsId}/non-associations")
  @Operation(
    summary = "Returns a single prisoners list of non associates.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul> <br> includeOpen is true by default, includeClosed is false by default. At least one must be true.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found prisoners non associates."),
      ApiResponse(
        responseCode = "400",
        description = "The HMPPS ID provided has an invalid format.",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPrisonersNonAssociates(
    @Parameter(description = "The HMPPS ID of the prisoner") @PathVariable hmppsId: String,
    @Parameter(description = "The prison ID of the prisoner") @PathVariable prisonId: String,
    @Parameter(description = "") @RequestParam(required = false, name = "includeOpen", defaultValue = "true") includeOpen: Boolean?,
    @Parameter(description = "") @RequestParam(required = false, name = "includeClosed", defaultValue = "false") includeClosed: Boolean?,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<NonAssociations?> {
    if (includeOpen == null && includeClosed == null || includeOpen == false && includeClosed == false) {
      throw ValidationException("includeOpen or includeClosed must be provided.")
    }

    val response = getPrisonersNonAssociationsService.execute(hmppsId, prisonId, includeOpen.toString(), includeClosed.toString(), filters)

    if (response.hasErrorCausedBy(ENTITY_NOT_FOUND, causedBy = UpstreamApi.NON_ASSOCIATIONS)) {
      throw EntityNotFoundException("Could not find prisoner with hmppsId: $hmppsId")
    }

    auditService.createEvent("GET_PRISONERS_NON_ASSOCIATES", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }
}
