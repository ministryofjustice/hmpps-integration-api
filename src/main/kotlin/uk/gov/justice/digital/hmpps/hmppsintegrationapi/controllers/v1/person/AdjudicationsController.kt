package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.HMPPS_ID
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.PAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.PER_PAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Adjudication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAdjudicationsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.paginateWith

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "default")
class AdjudicationsController(
  @Autowired val auditService: AuditService,
  @Autowired val getAdjudicationsForPersonService: GetAdjudicationsForPersonService,
) {
  @GetMapping("{encodedHmppsId}/reported-adjudications")
  @Operation(
    summary = "Returns adjudications associated with a person, sorted by dateTimeOfIncident (newest first).",
    responses = [
      ApiResponse(responseCode = "200", description = "OK"),
      ApiResponse(responseCode = "404", description = "Failed to find adjudications for the person with the provided hmppsId.", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonAdjudications(
    @Parameter(ref = HMPPS_ID) @PathVariable encodedHmppsId: String,
    @Parameter(ref = PAGE) @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @Parameter(ref = PER_PAGE) @RequestParam(required = false, defaultValue = "8", name = "perPage") perPage: Int,
  ): PaginatedResponse<Adjudication> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getAdjudicationsForPersonService.execute(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent("GET_PERSON_ADJUDICATIONS", mapOf("hmppsId" to hmppsId))
    return response.data.paginateWith(page, perPage)
  }
}
