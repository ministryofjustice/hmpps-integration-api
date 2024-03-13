package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.apache.catalina.connector.Connector
import org.apache.tomcat.util.buf.EncodedSolidusHandling
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.ServiceDownInterceptor

@Configuration
class TomcatConfig(
  @Autowired val serviceDownInterceptor: ServiceDownInterceptor,
) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(serviceDownInterceptor)
  }

  @Bean
  fun tomcatCustomizer(): WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    return WebServerFactoryCustomizer { factory: TomcatServletWebServerFactory ->
      factory.addConnectorCustomizers(
        TomcatConnectorCustomizer { connector: Connector ->
          connector.encodedSolidusHandling = EncodedSolidusHandling.PASS_THROUGH.value
        },
      )
    }
  }
}
