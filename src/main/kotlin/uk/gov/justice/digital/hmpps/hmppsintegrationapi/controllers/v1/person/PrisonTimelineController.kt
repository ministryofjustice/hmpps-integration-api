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
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.PRISON_TIMELINE_ENDPOINT_ENABLED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag.FeatureFlag
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiPrisonTimeline
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonTimelineForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1")
@Tags(Tag(name = "Persons"))
class PrisonTimelineController(
  @Autowired val getPrisonTimelineForPersonService: GetPrisonTimelineForPersonService,
  @Autowired val auditService: AuditService,
) {
  @FeatureFlag(name = PRISON_TIMELINE_ENDPOINT_ENABLED)
  @GetMapping("/persons/{hmppsId}/prison-timeline")
  @Operation(
    summary = "Returns prison timeline associated with a prisoner.",
    description =
      "Provides a summary of the periods this prisoner has been in prison. <br />" +
        "It includes the dates of each period, the prison and the reason for the movement. Each entry is divided into periods of time spent in prison separated by periods when the were out either via a release or a temporary absence (periods at court are not included). <br />" +
        "<b>Applicable filters</b>: <ul><li>prisons</li><li>supervisionStatuses</li></ul> <br />" +
        "<b>Outward Type be one of the following values:</b> <ul><li>ADM (Admission)</li><li>TAP (Temporary Absence)</li></ul>" +
        "<b>Inward Type be one of the following values:</b> <ul><li>ADM (Admission)</li><li>TAP (Temporary Absence)</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a prison timeline for a prisoner with the provided HMPPS Id"),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPrisonTimeline(
    @Parameter(description = "The HMPPS ID of the person", example = "A1234AA") @PathVariable hmppsId: String,
    @RequestAttribute requestContext: RequestContext?,
  ): DataResponse<PrisonApiPrisonTimeline> {
    val response = getPrisonTimelineForPersonService.getPrisonTimeline(hmppsId, requestContext)
    ensureResponse(hmppsId, response)

    auditService.createEvent("GET_PERSON_PRISON_TIMELINE", mapOf("hmppsId" to hmppsId))

    return DataResponse(response.data!!)
  }

  private fun ensureResponse(
    hmppsId: String,
    response: Response<PrisonApiPrisonTimeline?>,
  ) {
    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
  }
}
