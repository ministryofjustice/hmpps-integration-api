package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class Response<T>(
  val data: T,
  var errors: List<UpstreamApiError> = emptyList(),
) {
  companion object {
    fun <T> merge(responses: List<Response<List<T>>>): Response<List<T>> = Response(data = responses.flatMap { it.data }, errors = responses.flatMap { it.errors })
  }

  fun hasError(type: UpstreamApiError.Type): Boolean = this.errors.any { it.type == type }

  fun hasErrorCausedBy(
    type: UpstreamApiError.Type,
    causedBy: UpstreamApi,
  ): Boolean = this.errors.any { it.type == type && it.causedBy == causedBy }

  fun toResult(): ResponseResult<T & Any> {
    if (data == null || errors.isNotEmpty()) {
      return ResponseResult.Failure(this.errors)
    }
    return ResponseResult.Success(this.data)
  }
}

sealed class ResponseResult<out T> {
  data class Success<out T>(
    val data: T,
  ) : ResponseResult<T>()

  data class Failure(
    val errors: List<UpstreamApiError>,
  ) : ResponseResult<Nothing>()
}

data class DataResponse<T>(
  val data: T,
)
