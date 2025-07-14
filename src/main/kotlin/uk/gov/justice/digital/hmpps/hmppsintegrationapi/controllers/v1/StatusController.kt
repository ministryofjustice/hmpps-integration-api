package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Status

@RestController
@RequestMapping("/v1/status")
@Tag(name = "Service Status")
class StatusController {
  @Operation(
    summary = "Get service status.",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Service is running."),
    ],
  )
  @GetMapping
  fun getStatus(): Response<Status> = Response(data = Status())
}
