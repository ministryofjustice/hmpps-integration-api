package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.apache.logging.log4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

// Intercepts incoming requests and logs them
@Component
class LoggingInterceptor() : HandlerInterceptor {
  private val log: org.slf4j.Logger = LoggerFactory.getLogger(this::class.java)

  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    // log. allows us ot log at different levels
    if (log.isDebugEnabled)
      log.debug("Intercepted Request")

    if (log.isTraceEnabled)
      log.trace("Intercepted Request at TRACE level")

    // Informs the super to continue processing this request
    return true
  }
}
