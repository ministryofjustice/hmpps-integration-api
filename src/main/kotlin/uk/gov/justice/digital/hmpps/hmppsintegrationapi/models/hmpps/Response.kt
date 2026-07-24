package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.web.reactive.function.client.WebClientResponseException

data class Response<T>(
  val data: T,
  var errors: List<UpstreamApiError> = emptyList(),
) {
  companion object {
    fun <T> merge(responses: List<Response<List<T>>>): Response<List<T>> = Response(data = responses.flatMap { it.data }, errors = responses.flatMap { it.errors })

    fun <T> error(
      api: UpstreamApi,
      errors: List<Exception>,
      emptyValue: T,
    ): Response<T> = Response(emptyValue, wrapErrors(api, errors))
  }

  fun hasError(type: UpstreamApiError.Type): Boolean = this.errors.any { it.type == type }

  fun hasErrorCausedBy(
    type: UpstreamApiError.Type,
    causedBy: UpstreamApi,
  ): Boolean = this.errors.any { it.type == type && it.causedBy == causedBy }

  fun hasErrorCausedBy(type: UpstreamApiError.Type): Boolean = this.errors.any { it.type == type }
}

data class DataResponse<T>(
  val data: T,
)

inline fun <reified T> Response<T>.withoutNotFound() = Response(data = data, errors = errors.filter { it.type != UpstreamApiError.Type.ENTITY_NOT_FOUND })

fun wrapErrors(
  api: UpstreamApi,
  errors: List<Exception>,
): List<UpstreamApiError> = errors.map { mapError(it, api) }

fun mapError(
  error: Exception,
  api: UpstreamApi,
): UpstreamApiError =
  when (error) {
    is WebClientResponseException -> UpstreamApiError(api, mapStatus(error.statusCode), error.message)
    else -> UpstreamApiError(api, UpstreamApiError.Type.INTERNAL_SERVER_ERROR, error.message)
  }

fun mapStatus(status: HttpStatusCode): UpstreamApiError.Type =
  when (status) {
    HttpStatus.NOT_FOUND -> UpstreamApiError.Type.ENTITY_NOT_FOUND
    HttpStatus.BAD_REQUEST -> UpstreamApiError.Type.BAD_REQUEST
    HttpStatus.FORBIDDEN -> UpstreamApiError.Type.FORBIDDEN
    else -> UpstreamApiError.Type.INTERNAL_SERVER_ERROR
  }
