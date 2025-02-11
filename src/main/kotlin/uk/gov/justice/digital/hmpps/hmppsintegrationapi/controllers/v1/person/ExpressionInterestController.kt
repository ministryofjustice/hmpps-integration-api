package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully submitted an expression of interest"),
      ApiResponse(responseCode = "403", useReturnTypeSchema = true, description = "Access is forbidden"),
      ApiResponse(responseCode = "400", useReturnTypeSchema = true, description = "Bad Request"),
      ApiResponse(responseCode = "404", useReturnTypeSchema = true, description = "Not found"),
    ],
  )
  fun submitExpressionOfInterest(
    @Parameter(description = "A URL-encoded HMPPS identifier", example = "2008%2F0545166T") @PathVariable hmppsId: String,
    @Parameter(description = "A job identifier") @PathVariable jobid: String,
  ): ResponseEntity<Void> {
    putExpressionInterestService.sendExpressionOfInterest(hmppsId, jobid)

    return ResponseEntity.ok().build()
  }
}
