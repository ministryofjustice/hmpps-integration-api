package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import io.sentry.Sentry
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.AuthenticationFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException

@RestControllerAdvice
class HmppsIntegrationApiExceptionHandler {
  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: Exception): ResponseEntity<ErrorResponse> {
    log_and_capture("Validation exception: {}", e)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Validation failure: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(EntityNotFoundException::class)
  fun handle(e: EntityNotFoundException): ResponseEntity<ErrorResponse> {
    log_and_capture("Not found (404) returned with message {}", e)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "404 Not found error: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(AuthenticationFailedException::class)
  fun handleAuthenticationFailedException(e: AuthenticationFailedException): ResponseEntity<ErrorResponse?>? {
    log_and_capture("Authentication error: {}", e)
    return ResponseEntity
      .status(FORBIDDEN)
      .body(
        ErrorResponse(
          status = FORBIDDEN,
          userMessage = "Authentication error: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ResponseEntity<ErrorResponse?>? {
    log_and_capture("Unexpected exception", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR,
          userMessage = "Unexpected error: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  private fun log_and_capture(message: String, e: Exception) {
    log.error(message, e.message)
    Sentry.captureException(e)
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

data class ErrorResponse(
  val status: Int,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null,
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null,
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo)
}
