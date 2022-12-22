package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

import java.lang.RuntimeException

class AuthenticationFailedException(message: String) : RuntimeException(message)