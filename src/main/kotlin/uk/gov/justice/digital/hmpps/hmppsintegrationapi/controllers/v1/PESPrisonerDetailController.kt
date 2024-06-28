package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonIntegrationpes.PESPrisonerDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPESPrisonerDetailsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/pes/prisoner-details")
class PESPrisonerDetailController(
  @Autowired val getPESPersonDetailService: GetPESPrisonerDetailsService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("{hmppsId}")
  fun getPerson(
    @PathVariable hmppsId: String,
  ): Response<PESPrisonerDetails?> {
    val response = getPESPersonDetailService.execute(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not retrieve prisoner details for prisoner with id: $hmppsId")
    }

    auditService.createEvent(
      "GET_PES_PRISONER_INFORMATION",
      mapOf("hmppsId" to hmppsId),
    )
    return Response(
      data = response.data,
      errors = response.errors,
    )
  }
}
