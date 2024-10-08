package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetHmppsIdService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/hmpps/id")
@Tag(name = "default")
class HmppsIdController(
  @Autowired val getHmppsIdService: GetHmppsIdService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("nomis-number/{encodedNomisNumber}")
  fun getHmppsIdByNomisNumber(
    @PathVariable encodedNomisNumber: String,
  ): DataResponse<HmppsId?> {
    val nomisNumber = encodedNomisNumber.decodeUrlCharacters()

    val response = getHmppsIdService.execute(nomisNumber)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find HMPPS ID for nomis number: $nomisNumber")
    }

    auditService.createEvent("GET_HMPPS_ID_BY_NOMIS_NUMBER", mapOf("nomisNumber" to nomisNumber))

    return DataResponse(response.data)
  }
}
