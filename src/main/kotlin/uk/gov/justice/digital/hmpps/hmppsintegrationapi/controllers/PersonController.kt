package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

@RestController
@RequestMapping("/persons")
@Tag(name = "persons")
class PersonController(@Autowired val getPersonService: GetPersonService) {
  @Operation(summary = "Get person by ID.", description = "Returns a person.")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200", description = "Successfully found a person with the provided ID.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = Person::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "404", description = "Failed to find a person with the provided ID.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "500", description = "Unable to serve request.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      ),
    ]
  )
  @GetMapping("{id}")
  fun getPerson(@PathVariable id: String): Person? {
    val result = getPersonService.execute(id) ?: throw EntityNotFoundException("Could not find person with id: $id")

    return result
  }
}
