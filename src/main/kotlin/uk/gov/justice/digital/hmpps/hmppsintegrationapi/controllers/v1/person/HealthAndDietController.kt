package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HealthAndDiet
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetHealthAndDietService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "Persons")
class HealthAndDietController(
  @Autowired val auditService: AuditService,
  @Autowired val getHealthAndDietService: GetHealthAndDietService,
) {
  @Operation(
    summary = "Returns Health And Diet information for a person. It should be noted this data could be incomplete and checks as to whether data is valid and up to date should be taken.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a persons health and diet information."),
      ApiResponse(
        responseCode = "400",
        description = "The HMPPS ID provided has an invalid format.",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  @Tag(name = "Reception")
  @GetMapping("/{hmppsId}/health-and-diet")
  fun getHealthAndDiet(
    @Parameter(description = "The HMPPS ID of the person") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<HealthAndDiet?> {
    val response = getHealthAndDietService.execute(hmppsId, filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find information for id: $hmppsId")
    }
    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_HEALTH_AND_DIET_INFORMATION", mapOf("hmppsId" to hmppsId))

    return DataResponse(response.data)
  }
}
