package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LatestSentenceKeyDatesAndAdjustments
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetLatestSentenceKeyDatesAndAdjustmentsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetSentencesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.paginateWith

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "Persons")
class SentencesController(
  @Autowired val getSentencesForPersonService: GetSentencesForPersonService,
  @Autowired val getLatestSentenceKeyDatesAndAdjustmentsForPersonService: GetLatestSentenceKeyDatesAndAdjustmentsForPersonService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("{hmppsId}/sentences")
  @Operation(
    summary = "Returns sentences associated with a person, sorted by dateOfSentencing (newest first).",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found sentences for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonSentences(
    @Parameter(description = "The HMPPS ID of the person") @PathVariable hmppsId: String,
    @Parameter(description = "The page number (starting from 1)", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @Parameter(description = "The maximum number of results for a page", schema = Schema(minimum = "1")) @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
    @RequestAttribute filters: ConsumerFilters?,
  ): PaginatedResponse<Sentence> {
    val response = getSentencesForPersonService.execute(hmppsId, filters)

    if (response.hasErrorCausedBy(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_SENTENCES", mapOf("hmppsId" to hmppsId))
    return response.data.paginateWith(page, perPage)
  }

  @GetMapping("{encodedHmppsId}/sentences/latest-key-dates-and-adjustments")
  @Tags(value = [Tag("Reception"), Tag("Activities")])
  @Operation(
    summary = "Returns the key dates and adjustments about a person's release from prison for their latest sentence.",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found latest sentence key dates and adjustments for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "400", content = [Content(schema = Schema(ref = "#/components/schemas/BadRequest"))]),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonLatestSentenceKeyDatesAndAdjustments(
    @Parameter(description = "A URL-encoded HMPPS identifier", example = "2008%2F0545166T") @PathVariable encodedHmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<LatestSentenceKeyDatesAndAdjustments?> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId, filters)

    if (response.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.PRISON_API)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid or missing HMPPS ID")
    }

    auditService.createEvent(
      "GET_PERSON_SENTENCES_LATEST_KEY_DATES_AND_ADJUSTMENTS",
      mapOf("hmppsId" to hmppsId),
    )

    return DataResponse(response.data)
  }
}
