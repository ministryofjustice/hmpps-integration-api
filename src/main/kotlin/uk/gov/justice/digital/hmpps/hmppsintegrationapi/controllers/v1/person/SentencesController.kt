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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.HMPPS_ID
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.PAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.OpenAPIConfig.Companion.PER_PAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LatestSentenceKeyDatesAndAdjustments
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetLatestSentenceKeyDatesAndAdjustmentsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetSentencesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.paginateWith

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "persons")
class SentencesController(
  @Autowired val getSentencesForPersonService: GetSentencesForPersonService,
  @Autowired val getLatestSentenceKeyDatesAndAdjustmentsForPersonService: GetLatestSentenceKeyDatesAndAdjustmentsForPersonService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("{encodedHmppsId}/sentences")
  @Operation(
    summary = "Returns sentences associated with a person, sorted by dateOfSentencing (newest first).",
    responses = [
      ApiResponse(responseCode = "200", description = "Successfully found sentences for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonSentences(
    @Parameter(ref = HMPPS_ID) @PathVariable encodedHmppsId: String,
    @Parameter(ref = PAGE) @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @Parameter(ref = PER_PAGE) @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
  ): PaginatedResponse<Sentence> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getSentencesForPersonService.execute(hmppsId)

    if (response.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.NOMIS)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent("GET_PERSON_SENTENCES", mapOf("hmppsId" to hmppsId))
    return response.data.paginateWith(page, perPage)
  }

  @GetMapping("{encodedHmppsId}/sentences/latest-key-dates-and-adjustments")
  @Operation(
    summary = "Returns the key dates and adjustments about a person's release from prison for their latest sentence.",
    responses = [
      ApiResponse(responseCode = "200", description = "Successfully found latest sentence key dates and adjustments for a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonLatestSentenceKeyDatesAndAdjustments(
    @Parameter(ref = HMPPS_ID) @PathVariable encodedHmppsId: String,
  ): Response<LatestSentenceKeyDatesAndAdjustments?> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId)

    if (response.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.NOMIS)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent(
      "GET_PERSON_SENTENCES_LATEST_KEY_DATES_AND_ADJUSTMENTS",
      mapOf("hmppsId" to hmppsId),
    )
    return Response(response.data)
  }
}
