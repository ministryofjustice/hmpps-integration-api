package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonLicences
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetLicenceConditionService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons")
class LicenceConditionController(
  @Autowired val auditService: AuditService,
  @Autowired val getLicenceConditionService: GetLicenceConditionService,
) {
  @GetMapping("{encodedHmppsId}/licences/conditions")
  fun getLicenceConditions(
    @PathVariable encodedHmppsId: String,
  ): Map<String, PersonLicences?> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getLicenceConditionService.execute(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent("GET_PERSON_LICENCE_CONDITION", mapOf("hmppsId" to hmppsId))
    return mapOf("data" to response.data)
  }
}
