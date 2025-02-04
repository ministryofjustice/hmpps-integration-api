package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ForbiddenByUpstreamServiceException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.filters.CaseNoteFilter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCaseNotesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.paginateWith
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "default")
class CaseNotesController(
  @Autowired val getCaseNoteForPersonService: GetCaseNotesForPersonService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("{encodedHmppsId}/case-notes")
  @Operation(
    summary = "Returns case notes associated with a person.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found case notes for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getCaseNotesForPerson(
    @Parameter(description = "A URL-encoded HMPPS identifier", example = "2008%2F0545166T") @PathVariable encodedHmppsId: String,
    @Parameter(description = "Filter case notes from this date")
    @RequestParam(required = false, name = "startDate")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    startDate: LocalDateTime?,
    @Parameter(description = "Filter case notes up to this date")
    @RequestParam(required = false, name = "endDate")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    endDate: LocalDateTime?,
    @Parameter(description = "Filter by the location. example MDI", example = "MDI")
    @RequestParam(required = false, name = "locationId") locationId: String?,
    @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
  ): PaginatedResponse<CaseNote> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getCaseNoteForPersonService.execute(CaseNoteFilter(hmppsId, startDate, endDate, locationId))

    if (response.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.CASE_NOTES)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    if (response.hasErrorCausedBy(UpstreamApiError.Type.FORBIDDEN, causedBy = UpstreamApi.CASE_NOTES)) {
      throw ForbiddenByUpstreamServiceException("Upstream API Case Notes service returned a forbidden error")
    }

    auditService.createEvent("GET_CASE_NOTES", mapOf("hmppsId" to hmppsId))
    return response.data.paginateWith(page, perPage)
  }
}
