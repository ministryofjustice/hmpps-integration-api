package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetCommunityOffenderManagerForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val nDeliusGateway: NDeliusGateway,
  private val featureFlagConfig: FeatureFlagConfig,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<CommunityOffenderManager?> {
    val personResponse =
      if (filters?.hasPrisonFilter() == true || !featureFlagConfig.isEnabled(FeatureFlagConfig.PERSON_RESPONSIBLE_OFFICER_FIX_ENABLED)) {
        getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)
      } else {
        getPersonService.getPerson(hmppsId = hmppsId)
      }

    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val deliusCrn = personResponse.data?.identifiers?.deliusCrn ?: return Response(data = null)

    val nDeliusMappaDetailResponse = nDeliusGateway.getCommunityOffenderManagerForPerson(crn = deliusCrn)
    if (nDeliusMappaDetailResponse.errors.isNotEmpty() && !nDeliusMappaDetailResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      return Response(data = null, errors = nDeliusMappaDetailResponse.errors)
    }

    return Response(
      data = nDeliusMappaDetailResponse.data,
    )
  }
}
