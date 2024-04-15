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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCommunityOffenderManagerForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonOffenderManagerForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons")
class PersonResponsibleOfficerController(
  @Autowired val auditService: AuditService,
  @Autowired val getPrisonOffenderManagerForPersonService: GetPrisonOffenderManagerForPersonService,
  @Autowired val getCommunityOffenderManagerForPersonService: GetCommunityOffenderManagerForPersonService,
) {
  @GetMapping("{encodedHmppsId}/person-responsible-officer")
  fun getPersonResponsibleOfficer(
    @PathVariable encodedHmppsId: String,
  ): Map<String, PersonResponsibleOfficer> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val prisonOffenderManager = getPrisonOffenderManagerForPersonService.execute(hmppsId)
    val communityOffenderManager = getCommunityOffenderManagerForPersonService.execute(hmppsId)

    if (prisonOffenderManager.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find prison offender manager related to id: $hmppsId")
    }

    if (communityOffenderManager.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find community offender manager related to id: $hmppsId")
    }

    val mergedData =
      PersonResponsibleOfficer(
        prisonOffenderManager = prisonOffenderManager.data,
        communityOffenderManager = communityOffenderManager.data,
      )

    auditService.createEvent("GET_PERSON_RESPONSIBLE_OFFICER", mapOf("hmppsId" to hmppsId))
    return mapOf("data" to mergedData)
  }
}
