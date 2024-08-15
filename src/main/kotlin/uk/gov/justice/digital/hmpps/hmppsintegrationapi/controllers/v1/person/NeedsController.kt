package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.HMPPS_ID
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Needs
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetNeedsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "needs")
class NeedsController(
  @Autowired val getNeedsForPersonService: GetNeedsForPersonService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("{encodedHmppsId}/needs")
  @Operation(
    summary = """
      Returns criminogenic needs associated with a person. This endpoint does not serve LAO (Limited Access Offender) data.

      Note: Criminogenic needs are dynamic factors that are directly linked to criminal behaviour. Eight criminogenic needs are measured in OASys: Accommodation, Employability, Relationships, Lifestyle and Associates, Drug Misuse, Alcohol Misuse, Thinking & Behaviour and Attitudes. These are scored according to whether there is “no need”, “some need” or “severe need”, and a need is identified in a specific section based on calculations around these scores.
            However, the process by which needs are assessed is changing as early as next year (2024), specifically moving to a strength-based model that seeks to identify and develop the strengths of people with convictions. As a consequence of this, the information provided by this endpoint will also change.
    """,
    responses = [
      ApiResponse(responseCode = "200", description = "Successfully found criminogenic needs for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonNeeds(
    @Parameter(ref = HMPPS_ID) @PathVariable encodedHmppsId: String,
  ): Response<Needs?> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getNeedsForPersonService.execute(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent("GET_PERSON_NEED", mapOf("hmppsId" to hmppsId))
    return Response(response.data)
  }
}
