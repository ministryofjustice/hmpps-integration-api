package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.stream.Collectors

// Intercepts incoming requests and logs them
@Component
class RequestLogger() : HandlerInterceptor {
  private val log: org.slf4j.Logger = LoggerFactory.getLogger(this::class.java)

  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    // log. allows us ot log at different levels
    if (log.isDebugEnabled) {
      log.debug(RetrieveRequestData(request))
    }

    // Informs the super to continue processing this request
    return true
  }

  // Returns a loggable string with relevant request data
  fun RetrieveRequestData(request: HttpServletRequest): String {
    val requestIp: String = "New Request from " + request.remoteAddr
    val method: String = "Method: " + request.method
    val endpoint: String = "Request URI: " + request.requestURI // Could this expose authentication credentials?
    val requestURL: String = "Full Request URL: " + request.requestURL
    val body: String = "Body: " + request.reader.lines().collect(Collectors.joining()) ?: "NULL"

    return "$requestIp \n $method \n $endpoint \n $body \n $requestURL"
  }
}
