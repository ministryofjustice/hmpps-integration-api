package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

/**
 * This exception should be used where the requested upstream data exists, but the consumer's filters deny access to it"
 */
class FilterViolationException(
  message: String,
) : RuntimeException(message)
