package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

class MessageFailedException(
  msg: String,
  cause: Throwable? = null,
) : RuntimeException(msg, cause)
