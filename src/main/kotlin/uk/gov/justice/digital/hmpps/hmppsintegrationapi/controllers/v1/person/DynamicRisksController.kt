package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.LaoRedaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.LaoRedaction.Mode
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DynamicRisk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetDynamicRisksForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.paginateWith

@RestController
@RequestMapping("/v1/persons")
@Tags(Tag(name = "Persons"), Tag(name = "Alerts"))
class DynamicRisksController(
  @Autowired val getDynamicRisksForPersonService: GetDynamicRisksForPersonService,
  @Autowired val auditService: AuditService,
) {
  @LaoRedaction(Mode.REDACT)
  @GetMapping("{encodedHmppsId}/risks/dynamic")
  @Operation(
    summary = "Returns dynamic risks associated with a person.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found dynamic risks for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getDynamicRisks(
    @Parameter(description = "A URL-encoded HMPPS identifier", example = "2008%2F0545166T") @PathVariable encodedHmppsId: String,
    @Parameter(description = "The page number (starting from 1)", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @Parameter(description = "The maximum number of results for a page", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
  ): PaginatedResponse<DynamicRisk> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getDynamicRisksForPersonService.execute(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent("GET_DYNAMIC_RISKS", mapOf("hmppsId" to hmppsId))
    return response.data.paginateWith(page, perPage)
  }
}
