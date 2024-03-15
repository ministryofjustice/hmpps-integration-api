package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

@Component
class ServiceDownInterceptor : HandlerInterceptor {
  override fun postHandle(
    request: HttpServletRequest,
    response: HttpServletResponse,
    handler: Any,
    modelAndView: ModelAndView?,
  ) {
//    val resp = response.
    if (response.status >= 500) {
      response.resetBuffer()
      response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
      response.setHeader("Content-Type", "application/json")
      response.outputStream.print("{\"errorMessage\":\"Unable to complete request as an upstream service is not responding\"}")
      response.flushBuffer()
    }
  }
}
