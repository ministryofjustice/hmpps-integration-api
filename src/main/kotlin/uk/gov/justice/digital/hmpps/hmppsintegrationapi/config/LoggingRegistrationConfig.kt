package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class LoggingRegistrationConfig : WebMvcConfigurer {
  @Autowired
  lateinit var requestLogger: RequestLogger

  @Override
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(requestLogger)
  }
}
