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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetRiskCategoriesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "persons")
class RiskCategoriesController(
  @Autowired val getRiskCategoriesForPersonService: GetRiskCategoriesForPersonService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("{hmppsId}/risks/categories")
  @Operation(
    summary = "Returns the categories related to an offender.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found risk categories for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonRiskCategories(
    @Parameter(description = "The HMPPS ID of the person") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<RiskCategory?> {
    val response = getRiskCategoriesForPersonService.execute(hmppsId, filters)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_RISK_CATEGORIES", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }
}
