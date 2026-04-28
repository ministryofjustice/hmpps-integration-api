package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.telemetry

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService

@Configuration
class ClientTrackingConfiguration(
  private val clientTrackingInterceptor: ClientTrackingInterceptor,
) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(clientTrackingInterceptor).addPathPatterns("/**")
  }
}

@Component
class ClientTrackingInterceptor(
  private val telemetryService: TelemetryService,
) : HandlerInterceptor {
  override fun preHandle(
    request: HttpServletRequest,
    response: HttpServletResponse,
    handler: Any,
  ): Boolean {
    val subjectDistinguishedName = request.getAttribute("clientName") as String?
    subjectDistinguishedName?.let {
      telemetryService.setSpanAttribute("clientId", it)
    }
    // Set the certificate serial number in app insights
    val certificateSerialNumber = request.getAttribute("certificateSerialNumber") as String?
    certificateSerialNumber?.let {
      telemetryService.setSpanAttribute("certSerialNumber", it)
    }
    // Set on behalf off in app insights
    val onBehalfOf = request.getHeader("X-On-Behalf-Of")
    onBehalfOf?.let {
      telemetryService.setSpanAttribute("certOnBehalfOff", it)
    }
    return true
  }
}
