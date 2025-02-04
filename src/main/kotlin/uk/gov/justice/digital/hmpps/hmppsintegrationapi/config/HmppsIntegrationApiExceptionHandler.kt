package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import io.sentry.Sentry
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_GATEWAY
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ConflictFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.HmppsAuthFailedException

@RestControllerAdvice
class HmppsIntegrationApiExceptionHandler {
  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: Exception): ResponseEntity<ErrorResponse> {
    logAndCapture("Validation exception: {}", e)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          developerMessage = "Validation failure: ${e.message}",
          userMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(EntityNotFoundException::class)
  fun handle(e: EntityNotFoundException): ResponseEntity<ErrorResponse> {
    logAndCapture("Not found (404) returned with message {}", e)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          developerMessage = "404 Not found error: ${e.message}",
          userMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(HmppsAuthFailedException::class)
  fun handleAuthenticationFailedException(e: HmppsAuthFailedException): ResponseEntity<ErrorResponse?>? {
    logAndCapture("Authentication error: {}", e)
    return ResponseEntity
      .status(BAD_GATEWAY)
      .body(
        ErrorResponse(
          status = BAD_GATEWAY,
          developerMessage = "Authentication error: ${e.message}",
          userMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ResponseEntity<ErrorResponse?>? {
    logAndCapture("Unexpected exception", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR,
          developerMessage = "Unexpected error: ${e.message}",
          userMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(ConflictFoundException::class)
  fun handleConflictException(e: ConflictFoundException): ResponseEntity<ErrorResponse> {
    logAndCapture("Conflict exception: {}", e)
    return ResponseEntity
      .status(CONFLICT)
      .body(
        ErrorResponse(
          status = CONFLICT,
          developerMessage = "Unable to complete request as this is a duplicate request",
          userMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(WebClientResponseException::class)
  fun handleWebClientResponseException(e: WebClientResponseException): ResponseEntity<ErrorResponse?>? {
    logAndCapture("Upstream service down: {}", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR,
          developerMessage = "Unable to complete request as an upstream service is not responding",
          userMessage = e.message,
        ),
      )
  }

  private fun logAndCapture(
    message: String,
    e: Exception,
  ) {
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
