package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
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

  @GetMapping("{encodedHmppsId}/nomis-number")
  @Operation(
    summary = "Return NOMS number for a given hmpps Id",
    description = """Accepts a HMPPS Id (hmppsId) and looks up the corresponding NOMS number.
    """,
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true),
      ApiResponse(responseCode = "404", description = "NOMS number could not be found."),
      ApiResponse(responseCode = "400", description = "Invalid hmppsId."),
    ],
  )
  fun getNomisNumberByHMPPSID(
    @PathVariable encodedHmppsId: String,
  ): DataResponse<NomisNumber?> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()

    val response = getHmppsIdService.getNomisNumber(hmppsId)

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
