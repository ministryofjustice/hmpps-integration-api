package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecord
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.v2.PersonRecordService

@RestController
@RequestMapping("/v2/persons")
@Tag(name = "Person")
class PersonRecordController(
  private val auditService: AuditService,
  private val personRecordService: PersonRecordService,
) {
  @GetMapping("{hmppsId}")
  @Operation(
    summary = "Returns a core person record for a given HMPPS ID.",
    responses = [
      ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = CorePersonRecord::class))], description = "Successfully found a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPerson(
    @Parameter(description = "An HMPPS identifier (either a NOMIS ID or a CRN)", example = "A1234AA", required = true)
    @PathVariable("hmppsId") hmppsId: String,
    @RequestAttribute requestContext: RequestContext,
  ): DataResponse<CorePersonRecord> {
    val response = personRecordService.personRecord(hmppsId, requestContext)
    auditService.createEvent("GET_PERSON_RECORD", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }
}
