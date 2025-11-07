package uk.gov.justice.digital.hmpps.hmppsintegrationapi.domains.risk

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

@Service
class GetRiskCategoriesForPersonService(
    @Autowired val prisonApiGateway: PrisonApiGateway,
    @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<RiskCategory?> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)

    var personRiskCategories: Response<RiskCategory?> = Response(data = RiskCategory())

    if (personResponse.data?.nomisNumber != null) {
      personRiskCategories = prisonApiGateway.getRiskCategoriesForPerson(id = personResponse.data.nomisNumber)
    }

    return Response(
      data = personRiskCategories.data,
      errors = personResponse.errors + personRiskCategories.errors,
    )
  }
}
