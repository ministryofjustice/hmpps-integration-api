package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class ResponseException(
  override var message: String?,
  var statusCode: Int,
) : RuntimeException(message)
