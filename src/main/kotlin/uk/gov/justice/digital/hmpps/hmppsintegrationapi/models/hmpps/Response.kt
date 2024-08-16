package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class Response<T>(
  val data: T,
  val errors: List<UpstreamApiError> = emptyList(),
) {
  companion object {
    fun <T> merge(responses: List<Response<List<T>>>): Response<List<T>> = Response(data = responses.flatMap { it.data }, errors = responses.flatMap { it.errors })
  }

  fun hasError(type: UpstreamApiError.Type): Boolean = this.errors.any { it.type == type }

  fun hasErrorCausedBy(
    type: UpstreamApiError.Type,
    causedBy: UpstreamApi,
  ): Boolean = this.errors.any { it.type == type && it.causedBy == causedBy }
}

data class DataResponse<T>(val data: T)
