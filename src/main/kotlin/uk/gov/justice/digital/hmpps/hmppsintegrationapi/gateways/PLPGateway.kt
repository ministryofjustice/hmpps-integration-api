package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.education.EducationAssessmentSummaryResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActionPlanReviewsResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.InductionSchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.InductionSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReviewSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.plp.PLPPrisonerEducation

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

  fun getInductionScheduleHistory(prisonerNumber: String): Response<InductionSchedules> {
    val result =
      webClient.request<InductionSchedules>(
        HttpMethod.GET,
        "/inductions/$prisonerNumber/induction-schedule/history",
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
          data = InductionSchedules(listOf()),
          errors = result.errors,
        )
      }
    }
  }

  fun getReviewSchedules(prisonerNumber: String): Response<ReviewSchedules> {
    val result =
      webClient.request<ReviewSchedules>(
        HttpMethod.GET,
        "/action-plans/$prisonerNumber/reviews/review-schedules",
        authenticationHeader(),
        UpstreamApi.PLP,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        val reviewSchedules = result.data
        Response(data = reviewSchedules)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = ReviewSchedules(listOf()),
          errors = result.errors,
        )
      }
    }
  }

  fun getReviews(prisonerNumber: String): Response<ActionPlanReviewsResponse> {
    val result =
      webClient.request<ActionPlanReviewsResponse>(
        HttpMethod.GET,
        "/action-plans/$prisonerNumber/reviews",
        authenticationHeader(),
        UpstreamApi.PLP,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        val response = result.data
        Response(data = response)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = ActionPlanReviewsResponse(completedReviews = listOf()),
          errors = result.errors,
        )
      }
    }
  }

  fun getPrisonerEducation(prisonerNumber: String): Response<PLPPrisonerEducation?> {
    val result =
      webClient.request<PLPPrisonerEducation>(
        HttpMethod.GET,
        "/person/$prisonerNumber/education",
        authenticationHeader(),
        UpstreamApi.PLP,
      )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        return Response(
          data = result.data,
        )
      }
      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getEducationAssessmentSummary(prisonerNumber: String): Response<EducationAssessmentSummaryResponse?> {
    val result =
      webClient.request<EducationAssessmentSummaryResponse>(
        HttpMethod.GET,
        "/assessments/$prisonerNumber/required",
        authenticationHeader(),
        UpstreamApi.PLP,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }
      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
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
