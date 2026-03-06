package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.exceptions

open class PrisonNotFoundException(
  message: String,
) : UnmappableUrlException(message)
