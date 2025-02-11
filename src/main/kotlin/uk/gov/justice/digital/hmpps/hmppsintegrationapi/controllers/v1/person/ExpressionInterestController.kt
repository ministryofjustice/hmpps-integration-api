package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.PutExpressionInterestService

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "persons")
class ExpressionInterestController(
  @Autowired val putExpressionInterestService: PutExpressionInterestService,
) {
  @PutMapping("{hmppsId}/expression-of-interest/jobs/{jobid}")
  @Operation(
    summary = "Returns completed response",
    responses = [
      ApiResponse(
        responseCode = "200",
        useReturnTypeSchema = true,
        description = "Successfully submitted an expression of interest",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad Request",
        content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Access is forbidden",
        content = [Content(schema = Schema(ref = "#/components/schemas/ForbiddenResponse"))],
      ),
      ApiResponse(
        responseCode = "404",
        content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))],
      ),
    ],
  )
  fun submitExpressionOfInterest(
    @Parameter(description = "A HMPPS identifier", example = "A1234AA") @PathVariable hmppsId: String,
    @Parameter(description = "A job identifier") @PathVariable jobid: String,
  ): Response<Unit> {
    putExpressionInterestService.sendExpressionOfInterest(hmppsId, jobid)

    return Response(data = Unit, errors = emptyList())
  }
}
