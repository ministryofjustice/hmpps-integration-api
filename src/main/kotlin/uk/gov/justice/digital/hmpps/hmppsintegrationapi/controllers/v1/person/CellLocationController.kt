package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CellLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCellLocationForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons")
class CellLocationController(
  @Autowired val auditService: AuditService,
  @Autowired val getCellLocationForPersonService: GetCellLocationForPersonService,
) {
  @GetMapping("{encodedHmppsId}/cell-location")
  fun getPersonCellLocation(
    @PathVariable encodedHmppsId: String,
  ): Map<String, CellLocation?> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()

    val response = getCellLocationForPersonService.execute(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find cell location for id: $hmppsId")
    }

    auditService.createEvent("GET_PERSON_CELL_LOCATION", mapOf("hmppsId" to hmppsId))

    return mapOf("data" to response.data)
  }
}
