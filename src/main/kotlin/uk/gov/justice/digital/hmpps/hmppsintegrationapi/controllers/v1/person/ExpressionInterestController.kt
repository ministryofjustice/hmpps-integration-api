package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.expressionOfInterest.ExpressionInterest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ExpressionInterestService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "persons")
class ExpressionInterestController(
  @Autowired val getPersonService: GetPersonService,
  val expressionInterestService: ExpressionInterestService,
) {
  @PutMapping("{hmppsId}/expression-of-interest/jobs/{jobid}")
  @Operation(
    summary = "Returns completed response",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully submitted an expression of interest"),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/Error"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun submitExpressionOfInterest(
    @Parameter(description = "A URL-encoded HMPPS identifier", example = "2008%2F0545166T") @PathVariable hmppsId: String,
    @Parameter(description = "A job identifier") @PathVariable jobid: String,
  ): ResponseEntity<Map<String, String>> {
    val hmppsIdResponse = getPersonService.getCombinedDataForPerson(hmppsId)

    if (hmppsIdResponse.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH)) {
      return ResponseEntity.badRequest().build()
    }

    val nomisHmppsId = hmppsIdResponse.data.probationOffenderSearch?.hmppsId

    expressionInterestService.sendExpressionOfInterest(ExpressionInterest(jobid, nomisHmppsId))

    val responseBody = mapOf("message" to "Expression of interest submitted successfully")
    return ResponseEntity.ok(responseBody)
  }
}
