package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.filter.CommonsRequestLoggingFilter
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer



@Component
class LoggingRegistrationConfig : WebMvcConfigurer {
  @Autowired
  lateinit var loggingInterceptor: LoggingInterceptor

  @Override
  override fun addInterceptors(registry: InterceptorRegistry) {
    log.info("Adding logging interceptor")
    registry.addInterceptor(loggingInterceptor);
  }

  companion object{
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}