package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetImageService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@Tag(name = "images")
class ImageController(
  @Autowired val getImageService: GetImageService,
  @Autowired val auditService: AuditService,
  @Autowired val featureFlag: FeatureFlagConfig,
) {
  @GetMapping("/v1/persons/{hmppsId}/images/{id}")
  @Operation(
    summary = "Returns an image in bytes as a JPEG.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", description = "Successfully found an image with the provided ID.", content = [Content(mediaType = "image/jpeg", schema = Schema(type = "string", format = "binary"))]),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "404", description = "Failed to find an image with the provided ID.", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getImage(
    @PathVariable id: Int,
    @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): ResponseEntity<ByteArray> {
    if (!featureFlag.useImageEndpoints) {
      throw FeatureNotEnabledException(FeatureFlagConfig.USE_IMAGE_ENDPOINTS)
    }
    val response = getImageService.execute(id, hmppsId, filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find image with id: $id")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_IMAGE", mapOf("imageId" to id.toString()))

    return ResponseEntity
      .ok()
      .header("content-type", "image/jpeg")
      .body(response.data)
  }
}
