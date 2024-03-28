package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LatestSentenceKeyDatesAndAdjustments
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
class SentencesController(
  @Autowired val getSentencesForPersonService: GetSentencesForPersonService,
  @Autowired val getLatestSentenceKeyDatesAndAdjustmentsForPersonService: GetLatestSentenceKeyDatesAndAdjustmentsForPersonService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("{encodedHmppsId}/sentences")
  fun getPersonSentences(
    @PathVariable encodedHmppsId: String,
    @RequestParam(required = false, defaultValue = "1", name = "page") page: Int,
    @RequestParam(required = false, defaultValue = "10", name = "perPage") perPage: Int,
  ): PaginatedResponse<Sentence> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getSentencesForPersonService.execute(hmppsId)

    if (response.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.NOMIS)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent("GET_PERSON_SENTENCES", "Person sentence details with hmpps id: $hmppsId has been retrieved")
    return response.data.paginateWith(page, perPage)
  }

  @GetMapping("{encodedHmppsId}/sentences/latest-key-dates-and-adjustments")
  fun getPersonLatestSentenceKeyDatesAndAdjustments(
    @PathVariable encodedHmppsId: String,
  ): Map<String, LatestSentenceKeyDatesAndAdjustments?> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getLatestSentenceKeyDatesAndAdjustmentsForPersonService.execute(hmppsId)

    if (response.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.NOMIS)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent(
      "GET_PERSON_SENTENCES_LATEST_KEY_DATES_AND_ADJUSTMENTS",
      "The key dates and adjustments about a person’s release from prison for their latest sentence for persion with hmpps id: $hmppsId has been retrieved",
    )
    return mapOf("data" to response.data)
  }
}
