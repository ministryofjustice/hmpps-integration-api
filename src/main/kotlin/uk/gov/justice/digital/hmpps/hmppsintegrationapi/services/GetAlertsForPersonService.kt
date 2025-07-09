package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_ALERTS_API_FILTER
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerAlertsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedAlerts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonerAlerts.PAPaginatedAlerts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetAlertsForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val prisonerAlertsGateway: PrisonerAlertsGateway,
  @Autowired val featureConfig: FeatureFlagConfig,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
    page: Int,
    perPage: Int,
    pndOnly: Boolean = false,
  ): Response<PaginatedAlerts?> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)

    val useApiFilter = featureConfig.isEnabled(USE_ALERTS_API_FILTER)

    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val alertsResponse =
      if (useApiFilter && pndOnly) {
        prisonerAlertsGateway.getPrisonerAlerts(nomisNumber, page, size = perPage, PAPaginatedAlerts.PND_ALERT_CODES)
      } else {
        prisonerAlertsGateway.getPrisonerAlerts(nomisNumber, page, size = perPage)
      }

    if (alertsResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = alertsResponse.errors)
    }

    return Response(
      data = alertsResponse.data?.toPaginatedAlerts(pndOnly, useApiFilter),
    )
  }
}
