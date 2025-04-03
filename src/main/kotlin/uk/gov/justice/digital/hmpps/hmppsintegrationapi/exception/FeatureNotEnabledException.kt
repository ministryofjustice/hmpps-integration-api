package uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception

import org.springframework.http.HttpStatus

fun featureNotEnabledException(feature: String) = ResponseException("$feature not enabled", HttpStatus.SERVICE_UNAVAILABLE.value())
