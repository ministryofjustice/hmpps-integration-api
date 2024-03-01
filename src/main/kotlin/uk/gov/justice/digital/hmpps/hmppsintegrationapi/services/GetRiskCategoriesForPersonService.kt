package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskCategory

@Service
class GetRiskCategoriesForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<RiskCategory> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val deliusCrn = personResponse.data?.identifiers?.deliusCrn

    var personRiskCategories: Response<RiskCategory> = Response(data = RiskCategory())

    if (deliusCrn != null) {
      personRiskCategories = nomisGateway.getRiskCategoriesForPerson(id = deliusCrn)
    }

    return Response(
      data = personRiskCategories.data,
      errors = personResponse.errors + personRiskCategories.errors,
    )
  }
}
