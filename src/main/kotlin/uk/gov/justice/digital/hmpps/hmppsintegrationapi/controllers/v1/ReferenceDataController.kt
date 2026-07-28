package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.COURTS_ENDPOINT_ENABLED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag.FeatureFlag
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Court
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReferenceData
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCourtService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ReferenceDataService

@RestController
@RequestMapping("/v1/hmpps/reference-data")
@Tag(name = "Reference Data")
class ReferenceDataController(
  var referenceDataService: ReferenceDataService,
  var getCourtService: GetCourtService,
) {
  @GetMapping
  @Operation(
    summary = """Returns probation and prison reference data codes descriptions for values returned by the API""",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully returned prison and probation reference data."),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getReferenceData(): Response<ReferenceData?> = referenceDataService.referenceData()

  @FeatureFlag(name = COURTS_ENDPOINT_ENABLED)
  @GetMapping("courts/{courtId}")
  @Operation(
    summary = """Returns information about a court""",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully returned court reference data."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/NotFoundError"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getCourt(
    @Parameter(description = "Court ID", example = "ACCRYC") @PathVariable courtId: String,
    @RequestAttribute requestContext: RequestContext?,
  ): Response<Court?> {
    val response = getCourtService.getCourt(courtId, requestContext)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find court with id: $courtId")
    }

    return response
  }
}
