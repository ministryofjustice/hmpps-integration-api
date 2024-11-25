package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.InductionSchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReviewSchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Component
class PLPGateway(
  @Value("\${services.plp.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getInductionSchedule(prisonerNumber: String): Response<InductionSchedule> {
    val result =
      webClient.request<InductionSchedule>(
        HttpMethod.GET,
        "/inductions/$prisonerNumber/induction-schedule",
        authenticationHeader(),
        UpstreamApi.PLP,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        val inductionSchedule = result.data
        Response(data = inductionSchedule)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = InductionSchedule(),
          errors = result.errors,
        )
      }
    }
  }

  fun getReviewSchedule(prisonerNumber: String): Response<ReviewSchedule> {
    val result =
      webClient.request<ReviewSchedule>(
        HttpMethod.GET,
        "/inductions/$prisonerNumber/review-schedule",
        authenticationHeader(),
        UpstreamApi.PLP,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        val inductionSchedule = result.data
        Response(data = inductionSchedule)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = ReviewSchedule(),
          errors = result.errors,
        )
      }
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("PLP")
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
