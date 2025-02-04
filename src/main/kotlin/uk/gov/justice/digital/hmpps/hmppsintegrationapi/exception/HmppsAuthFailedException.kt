package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_GATEWAY)
class HmppsAuthFailedException(
  message: String,
) : RuntimeException(message)
