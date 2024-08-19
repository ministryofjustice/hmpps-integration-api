package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "default")
class EPFPersonDetailController(
  @Autowired val getEPFPersonDetailService: GetEPFPersonDetailService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("{hmppsId}/{eventNumber}")
  @Operation(
    summary = "Probation case information for the Effective Proposals Framework service",
    description = """
      <p>Accepts a Hmpps Id (hmppsId) and Delius Event number
      and returns a data structure giving background information on the probation case
      for use in the Effective Proposals Framework system. The information is used to
      reduce the need for the EPF user to re-key information already held in Delius.</p>
    """,
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
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
