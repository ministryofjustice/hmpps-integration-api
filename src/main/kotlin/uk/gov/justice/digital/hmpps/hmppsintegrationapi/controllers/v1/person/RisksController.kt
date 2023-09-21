package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Risks
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetRisksForPersonService

@RestController
@RequestMapping("/v1/persons")
class RisksController(
  @Autowired val getRisksForPersonService: GetRisksForPersonService,
) {

  @GetMapping("{encodedPncId}/risks")
  fun getPersonRisks(@PathVariable encodedPncId: String): Map<String, Risks?> {
    val pncId = encodedPncId.decodeUrlCharacters()
    val response = getRisksForPersonService.execute(pncId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $pncId")
    }

    return mapOf("data" to response.data)
  }
}
