package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

class LimitedAccessFailedException(
  msg: String = "LAO Check failed",
) : RuntimeException(msg)
