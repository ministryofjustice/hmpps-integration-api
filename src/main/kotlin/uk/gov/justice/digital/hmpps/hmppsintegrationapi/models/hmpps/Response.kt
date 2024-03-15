package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps


data class Response<T>(val data: T, override val errors: List<UpstreamApiError> = emptyList()) : BaseResponse(errors) {

  companion object {
    fun <T> merge(responses: List<Response<List<T>>>): Response<List<T>> =
      Response(data = responses.flatMap { it.data }, errors = responses.flatMap { it.errors })
  }
}
