package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

class LaoConfigurationException(
  path: String,
) : RuntimeException("LAO check configured for $path but no hmppsId was found")
