package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetEPFPersonDetailService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/epf/person-details")
class EPFPersonDetailController(
  @Autowired val getEPFPersonDetailService: GetEPFPersonDetailService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("{hmppsId}/{eventNumber}")
  fun getCaseDetail(
    @PathVariable hmppsId: String,
    @PathVariable eventNumber: Int,
  ): Response<CaseDetail?> {
    val response = getEPFPersonDetailService.execute(hmppsId, eventNumber)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("The case details for this person cannot be retrieved. Please ensure the CRN and event number are correct and If the person has Limited Access Only (LAO) applied to their NDelius record, then it will not be possible to retrieve their details. In such circumstances, please enter the person's details manually")
    }

    auditService.createEvent(
      "GET_EPF_PROBATION_CASE_INFORMATION",
      mapOf("hmppsId" to hmppsId, "eventNumber" to eventNumber.toString()),
    )
    return Response(
      data = response.data,
      errors = response.errors,
    )
  }
}
