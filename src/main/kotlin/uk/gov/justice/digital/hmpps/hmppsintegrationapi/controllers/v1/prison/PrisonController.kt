package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.prison

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError.Type.ENTITY_NOT_FOUND
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/prison")
@Tag(name = "prison")
class PrisonController(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("/prisoners/{hmppsId}")
  @Operation(
    summary = "Returns a single prisoners details given an hmppsId, does not query for a probation person.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a prisoner with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPerson(
    @PathVariable hmppsId: String,
  ): DataResponse<Person?> {
    val response = getPersonService.getPrisoner(hmppsId)

    if (response.hasErrorCausedBy(ENTITY_NOT_FOUND, causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH)) {
      throw EntityNotFoundException("Could not find person with hmppsId: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_DETAILS", mapOf("hmppsId" to hmppsId))
    val data = response.data
    return DataResponse(data)
  }
}
