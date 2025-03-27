package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetHmppsIdService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/hmpps/id")
@Tag(name = "default")
class HmppsIdController(
  @Autowired val getHmppsIdService: GetHmppsIdService,
  @Autowired val auditService: AuditService,
  @Autowired val getPersonService: GetPersonService,
) {
  @GetMapping("nomis-number/{nomisNumber}", "by-nomis-number/{nomisNumber}")
  @Operation(
    summary = "Return a HMPPS id for a given nomis number",
    description = "<b>Applicable filters</b>: <ul><li>prisons</li></ul>",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true),
      ApiResponse(responseCode = "404", description = "Nomis number could not be found."),
      ApiResponse(responseCode = "400", description = "Invalid hmppsId."),
    ],
  )
  fun getHmppsIdByNomisNumber(
    @PathVariable nomisNumber: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<HmppsId?> {
    val response = getHmppsIdService.execute(nomisNumber, filters)

    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid Nomis number: $nomisNumber")
    }

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find HMPPS ID for Nomis number: $nomisNumber")
    }

    auditService.createEvent("GET_HMPPS_ID_BY_NOMIS_NUMBER", mapOf("nomisNumber" to nomisNumber))

    return DataResponse(response.data)
  }

  @GetMapping("nomis-number/by-hmpps-id/{hmppsId}")
  @Operation(
    summary = "Return nomis number for a given HMPPS Id",
    description = """
      Accepts a HMPPS Id (hmppsId) and looks up the corresponding nomis number.<br>
      <b>Applicable filters</b>: <ul><li>prisons</li></ul>
    """,
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true),
      ApiResponse(responseCode = "404", description = "Nomis number could not be found."),
      ApiResponse(responseCode = "400", description = "Invalid hmppsId."),
    ],
  )
  fun getNomisNumberByHMPPSID(
    @PathVariable hmppsId: String,
    @RequestAttribute filters: ConsumerFilters?,
  ): DataResponse<NomisNumber?> {
    val response = getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)
    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find nomis number for HMPPS ID: $hmppsId")
    }
    if (response.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    auditService.createEvent("GET_NOMIS_NUMBER_BY_HMPPS_ID", mapOf("hmppsId" to hmppsId))

    return DataResponse(response.data)
  }
}
