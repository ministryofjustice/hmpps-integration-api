package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.telemetry

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class ClientTrackingConfiguration(
  private val clientTrackingInterceptor: ClientTrackingInterceptor,
) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(clientTrackingInterceptor).addPathPatterns("/**")
  }
}

@Component
class ClientTrackingInterceptor : HandlerInterceptor {
  override fun preHandle(
    request: HttpServletRequest,
    response: HttpServletResponse,
    handler: Any,
  ): Boolean {
    val subjectDistinguishedName = request.getAttribute("clientName") as String?
    subjectDistinguishedName?.let {
      try {
        Span.current().setAttribute("clientId", it)
      } catch (ignored: Exception) {
        // Do nothing - don't create client id span
      }
    }
    return true
  }
}
