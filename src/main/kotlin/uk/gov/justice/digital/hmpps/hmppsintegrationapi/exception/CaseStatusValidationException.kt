package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

class CaseStatusValidationException(
  override val message: String,
) : RuntimeException(message)
