package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.GONE)
class DeprecatedApiException(
  message: String = "The API has been deprecated.",
) : RuntimeException(message)
