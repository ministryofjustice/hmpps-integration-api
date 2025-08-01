package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import io.sentry.Sentry
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_GATEWAY
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ConflictFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ForbiddenByUpstreamServiceException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.HmppsAuthFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException

@RestControllerAdvice
class HmppsIntegrationApiExceptionHandler {
  @ExceptionHandler(value = [ValidationException::class, HttpMessageNotReadableException::class])
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

  // Exceptions thrown by the @Valid annotation on request body
  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
    logAndCapture("Validation issues in request body: {}", e)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ValidationErrorResponse(
          developerMessage = "Validation issues in request body",
          userMessage = "Validation issues in request body",
          validationErrors = e.allErrors.mapNotNull { it.defaultMessage },
        ),
      )
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException::class)
  fun handleTypeMismatch(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
    logAndCapture("Type mismatch for parameter '${e.name}'", e)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse(
          status = HttpStatus.BAD_REQUEST,
          developerMessage = "Type mismatch: ${e.message}",
          userMessage = "Invalid input type for '${e.name}'",
        ),
      )
  }

  @ExceptionHandler(HandlerMethodValidationException::class)
  fun handleHandlerMethodValidationException(e: HandlerMethodValidationException): ResponseEntity<ValidationErrorResponse> {
    logAndCapture("Validation issues in request body: {}", e)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ValidationErrorResponse(
          developerMessage = "Validation issues in request body",
          userMessage = "Validation issues in request body",
          validationErrors = e.allErrors.mapNotNull { it.defaultMessage },
        ),
      )
  }

  // Custom exceptions

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
    logAndCapture("Authentication error in HMPPS Auth: {}", e)
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

  @ExceptionHandler(ForbiddenByUpstreamServiceException::class, LimitedAccessException::class)
  fun handleAuthenticationFailedException(e: ForbiddenByUpstreamServiceException): ResponseEntity<ErrorResponse?>? {
    logAndCapture("Forbidden to complete action by upstream service: {}", e)
    return ResponseEntity
      .status(FORBIDDEN)
      .body(
        ErrorResponse(
          status = FORBIDDEN,
          developerMessage = "Forbidden to complete action by upstream service: ${e.message}",
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

  @ExceptionHandler(MessageFailedException::class)
  fun handleMessageFailedException(e: MessageFailedException): ResponseEntity<ErrorResponse?>? {
    logAndCapture("Message failed to be added to queue: {}", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR,
          developerMessage = "Failed to add message to queue.",
          userMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(FeatureNotEnabledException::class)
  fun handleFeatureNotEnabledException(e: FeatureNotEnabledException): ResponseEntity<ErrorResponse> {
    logAndCapture("Validation exception: {}", e)
    return ResponseEntity
      .status(SERVICE_UNAVAILABLE)
      .body(
        ErrorResponse(
          status = SERVICE_UNAVAILABLE,
          developerMessage = e.message,
          userMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(MissingServletRequestParameterException::class)
  fun handleFMissingServletRequestParameterException(e: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> {
    logAndCapture("Missing request parameter exception: {}", e)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          developerMessage = e.message,
          userMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(LimitedAccessFailedException::class)
  fun handleLimitedAccessFailedException(e: LimitedAccessFailedException): ResponseEntity<ErrorResponse> {
    logAndCapture("Limited access failure exception: {}", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR,
          developerMessage = e.message,
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

data class ValidationErrorResponse(
  val status: Int,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val validationErrors: List<String>,
) {
  constructor(
    userMessage: String? = null,
    developerMessage: String? = null,
    validationErrors: List<String>,
  ) :
    this(BAD_REQUEST.value(), userMessage, developerMessage, validationErrors)
}
