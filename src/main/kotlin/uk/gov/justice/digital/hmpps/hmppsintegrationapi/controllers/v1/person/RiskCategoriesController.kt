package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.decodeUrlCharacters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetRiskCategoriesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/persons")
class RiskCategoriesController(
  @Autowired val getRiskCategoriesForPersonService: GetRiskCategoriesForPersonService,
  @Autowired val auditService: AuditService,
) {

  @GetMapping("{encodedHmppsId}/risks/categories")
  fun getPersonRiskCategories(
    @PathVariable encodedHmppsId: String,

  ): Map<String, RiskCategory?> {
    val hmppsId = encodedHmppsId.decodeUrlCharacters()
    val response = getRiskCategoriesForPersonService.execute(hmppsId)

    if (response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }
    auditService.createEvent("GET_PERSON_RISK_CATEGORIES", "Person risk categories with hmpps id: $hmppsId has been retrieved")
    return mapOf("data" to response.data)
  }
}
