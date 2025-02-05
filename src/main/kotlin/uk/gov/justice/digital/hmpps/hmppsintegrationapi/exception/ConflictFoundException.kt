package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class ConflictFoundException(
  message: String,
) : RuntimeException(message)
