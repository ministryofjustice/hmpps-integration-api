package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonResponsibleOfficer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonOfficerManagerForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons")
class PersonResponsibleOfficerController(
  @Autowired val auditService: AuditService,
  @Autowired val getPrisonOfficerManagerForPersonService: GetPrisonOfficerManagerForPersonService,
) {
  @GetMapping("{encodedHmppsId}/person-responsible-officer")
  fun getPersonResponsibleOfficer(
    @PathVariable encodedHmppsId: String,
  ): Map<String, PersonResponsibleOfficer> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val prisonOfficerManager = getPrisonOfficerManagerForPersonService.execute(hmppsId)
//    val communityOfficerManager = getCommunityOfficerManagerForPersonService.execute(hmppsId)

    if (prisonOfficerManager.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find prison officer manager related to id: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_RESPONSIBLE_OFFICER", mapOf("hmppsId" to hmppsId))
//      return mapOf("data" to response.data)
    return mapOf("data" to PersonResponsibleOfficer())
  }
}
