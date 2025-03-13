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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonResponsibleOfficer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCommunityOffenderManagerForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonOffenderManagerForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "default")
class PersonResponsibleOfficerController(
  @Autowired val auditService: AuditService,
  @Autowired val getPrisonOffenderManagerForPersonService: GetPrisonOffenderManagerForPersonService,
  @Autowired val getCommunityOffenderManagerForPersonService: GetCommunityOffenderManagerForPersonService,
) {
  @GetMapping("{hmppsId}/person-responsible-officer")
  @Operation(
    summary = "Returns the person responsible officer associated with a person.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found the person responsible officer for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonResponsibleOfficer(
    @Parameter(description = "A HMPPS identifier") @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<PersonResponsibleOfficer> {
    val prisonOffenderManager = getPrisonOffenderManagerForPersonService.execute(hmppsId, filters)
    val communityOffenderManager = getCommunityOffenderManagerForPersonService.execute(hmppsId, filters)

    if (prisonOffenderManager.hasError(UpstreamApiError.Type.BAD_REQUEST) || communityOffenderManager.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    if (prisonOffenderManager.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find prison offender manager related to id: $hmppsId")
    }

    if (communityOffenderManager.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find community offender manager related to id: $hmppsId")
    }

    val mergedData =
      PersonResponsibleOfficer(
        prisonOffenderManager = prisonOffenderManager.data,
        communityOffenderManager = communityOffenderManager.data,
      )

    auditService.createEvent("GET_PERSON_RESPONSIBLE_OFFICER", mapOf("hmppsId" to hmppsId))
    return DataResponse(mergedData)
  }
}
