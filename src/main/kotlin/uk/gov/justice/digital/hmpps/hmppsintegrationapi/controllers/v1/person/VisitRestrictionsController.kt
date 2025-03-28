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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonVisitRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContactRestrictions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitRestrictionsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitorRestrictionsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons/{hmppsId}")
@Tag(name = "persons")
class VisitRestrictionsController(
  @Autowired val auditService: AuditService,
  @Autowired val getVisitRestrictionsForPersonService: GetVisitRestrictionsForPersonService,
  @Autowired val getVisitorRestrictionsService: GetVisitorRestrictionsService,
) {
  @Operation(
    summary = "Gets visit restrictions for a prisoner.",
    description = "Returns a prisoner's visit restrictions. Only returns the visit restrictions for the prisoner's most recent booking.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a person's visit restrictions with the provided HMPPS ID."),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  @GetMapping("/visit-restrictions")
  fun getRestrictionsForPerson(
    @Parameter(description = "A HMPPS identifier") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<List<PersonVisitRestriction>?> {
    val response = getVisitRestrictionsForPersonService.execute(hmppsId, filters = filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Bad request from upstream ${response.errors.first().description}")
    }
    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_VISIT_RESTRICTIONS", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }

  @Operation(
    summary = "Get the restrictions for a visitor.",
    description = "Provides both the global restrictions for the visitor, as well as the restrictions for the relationship between prisoner and visitor.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a prisoners vistor restrictions with the provided HMPPS ID and contact ID."),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  @GetMapping("/visitor/{contactId}/restrictions")
  fun getVisitorRestrictions(
    @Parameter(description = "A HMPPS identifier") @PathVariable hmppsId: String,
    @Parameter(description = "A contact ID") @PathVariable contactId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<PrisonerContactRestrictions?> {
    val stringifiedContactId = contactId.toLongOrNull() ?: throw ValidationException("Invalid contact ID")
    val response = getVisitorRestrictionsService.execute(hmppsId, stringifiedContactId, filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Bad request from upstream ${response.errors.first().description}")
    }
    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find prisoner with visitor restrictions(hmmpsId, contactId): $hmppsId $contactId")
    }

    auditService.createEvent("GET_VISITOR_RESTRICTIONS", mapOf("hmppsId" to hmppsId, "contactId" to contactId))
    return DataResponse(response.data)
  }
}
