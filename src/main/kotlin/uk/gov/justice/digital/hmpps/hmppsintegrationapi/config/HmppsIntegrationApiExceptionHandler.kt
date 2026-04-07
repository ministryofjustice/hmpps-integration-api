package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import io.sentry.Sentry
import io.sentry.SentryLevel
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException

// Jess -- this entier file looks to be 99% unused. Only one function is ued.
@RestControllerAdvice
class HmppsIntegrationApiExceptionHandler {
  // Custom exceptions

  @ExceptionHandler(EntityNotFoundException::class)
  fun handle(e: EntityNotFoundException): ResponseEntity<ErrorResponse> {
    logInfo("Not found (404) returned with message {}", e, false)
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

  /**
   * Logs, and records in Sentry, an informational message.
   */
  private fun logInfo(
    message: String,
    e: Exception,
    sendToSentry: Boolean = true,
  ) {
    log.info(message, e)
    if (sendToSentry) {
      Sentry.captureMessage("$message : ${e.message}", SentryLevel.INFO)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val traceId = "${Sentry.getSpan()?.traceContext()?.traceId}"
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
