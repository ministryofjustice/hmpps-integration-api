package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

import java.lang.RuntimeException

class AuthorisationFailedException(message: String) : RuntimeException(message)
