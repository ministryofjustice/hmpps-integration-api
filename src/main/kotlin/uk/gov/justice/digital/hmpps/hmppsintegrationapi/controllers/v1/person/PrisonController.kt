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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.*
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisPersonAccount
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonPrisonAccountService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v0/prison")
@Tag(name = "prison")
class PrisonController(@Autowired val auditService: AuditService,
                       @Autowired val getPersonPrisonAccount: GetPersonPrisonAccountService
) {
  @GetMapping("{encodedHmppsId}")
  @Operation(
    summary = "Returns a person",
    responses = [
      ApiResponse(responseCode = "200", useReturnTypeSchema = true, description = "Successfully found a person with the provided HMPPS ID."),
      ApiResponse(responseCode = "404", content = [Content(schema = Schema(ref = "#/components/schemas/PersonNotFound"))]),
      ApiResponse(responseCode = "500", content = [Content(schema = Schema(ref = "#/components/schemas/InternalServerError"))]),
    ],
  )
  fun getPersonsPrison(
    @Parameter(description = "A URL-encoded HMPPS identifier", example = "2008%2F0545166T") @PathVariable encodedHmppsId: String,
    ) : DataResponse<NomisPersonAccount?> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()

    val response = getPersonPrisonAccount.execute(hmppsId)

    if (response.data.cash == null) {
      throw EntityNotFoundException("Could not find nomis number with supplied hmppsId: $hmppsId")
    }

    auditService.createEvent("EXAMPLE_EVENT_BLA_BLA", mapOf("hmppsId" to hmppsId))
    return DataResponse(response.data)
  }
}
