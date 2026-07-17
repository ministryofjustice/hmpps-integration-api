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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Needs
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetNeedsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons")
@Tags(value = [Tag(name = "Persons"), Tag(name = "Needs")])
class NeedsController(
  @Autowired val getNeedsForPersonService: GetNeedsForPersonService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("{hmppsId}/needs")
  @Operation(
    summary = "Returns the criminogenic needs and non-criminogenic needs associated with a person.",
    description =
        "Criminogenic needs are dynamic factors that are directly linked to criminal behaviour.<br /> <br />" +
        "Eight criminogenic needs are measured in OASys: Accommodation, Employability, Relationships, Lifestyle and Associates, Drug Misuse, Alcohol Misuse, Thinking & Behaviour and Attitudes.<br />" +
        "Non-criminogenic needs are individual traits, personal issues, or life challenges that cause personal distress or require support but have no direct influence on an individual's likelihood of committing a crime or reoffending.<br /><br />" +
        "Two non-criminogenic are held in OASys: Finance and Emotional Well-being",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found needs for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonNeeds(
    @Parameter(description = "HMPPS identifier", example = "A1234AA") @PathVariable hmppsId: String,
  ): DataResponse<Needs?> {
    val response = getNeedsForPersonService.execute(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent("GET_PERSON_NEED", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }
}
