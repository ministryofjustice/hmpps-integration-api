package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAddressesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "persons")
class AddressController(
  @Autowired val auditService: AuditService,
  @Autowired val getAddressesForPersonService: GetAddressesForPersonService,
) {
  @GetMapping("{encodedHmppsId}/addresses")
  @Operation(
    summary = "Returns addresses associated with a person, ordered by startDate.",
    responses = [
      ApiResponse(responseCode = "200", description = "Successfully found a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonAddresses(
    @Parameter(description = "A URL-encoded HMPPS identifier", example = "2008%2F0545166T") @PathVariable encodedHmppsId: String,
  ): DataResponse<List<Address>> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getAddressesForPersonService.execute(hmppsId)

    if (
      response.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.NOMIS) &&
      response.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH)
    ) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent("GET_PERSON_ADDRESS", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }
}
