package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.adjudications.ReportedAdjudication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Adjudication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Component
class AdjudicationsGateway(
  @Value("\${services.adjudications.base-url}") baseUrl: String,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "Enables adjudication charge creation, hearing scheduling and awards on DPS.",
      developerPortalId = "DPS001",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/products/adjudications-1",
      apiDocUrl = "https://manage-adjudications-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html",
      apiSpecUrl = "https://manage-adjudications-api-dev.hmpps.service.justice.gov.uk/v3/api-docs",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-manage-adjudications-api",
    )

  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getReportedAdjudicationsForPerson(id: String): Response<List<Adjudication>> {
    val result =
      webClient.requestList<ReportedAdjudication>(
        HttpMethod.GET,
        "/reported-adjudications/prisoner/$id",
        authenticationHeader(),
        UpstreamApi.ADJUDICATIONS,
      )

    return when (result) {
      is WebClientWrapper.WebClientWrapperResponse.Success -> {
        Response(
          data =
            result.data
              .map { it.toAdjudication() }
              .sortedByDescending { it.incidentDetails?.dateTimeOfIncident },
        )
      }

      is WebClientWrapper.WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("Adjudications")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
