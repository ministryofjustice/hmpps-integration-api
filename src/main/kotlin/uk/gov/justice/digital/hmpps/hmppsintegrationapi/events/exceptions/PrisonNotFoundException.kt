package uk.gov.justice.digital.hmpps.hmppsintegrationevents.exceptions

open class PrisonNotFoundException(
  message: String,
) : UnmappableUrlException(message)
