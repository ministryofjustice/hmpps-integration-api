package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetEPFPersonDetailService

@RestController
@RequestMapping("/v1/epf/person-details")
class EPFPersonDetailController(
  @Autowired val getEPFPersonDetailService: GetEPFPersonDetailService,
) {

  @GetMapping("{hmppsId}/{eventNumber}")
  fun getCaseDetail(
    @PathVariable hmppsId: String,
    @PathVariable eventNumber: Int,
  ): Response<CaseDetail?> {
    val response = getEPFPersonDetailService.execute(hmppsId, eventNumber)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not retrieve person details for person with id: $hmppsId")
    }

    return Response(
      data = response.data,
      errors = response.errors,
    )
  }
}
