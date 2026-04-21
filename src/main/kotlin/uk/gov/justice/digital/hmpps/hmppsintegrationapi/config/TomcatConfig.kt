package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.apache.catalina.connector.Connector
import org.apache.tomcat.util.buf.EncodedSolidusHandling
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TomcatConfig {
  @Bean
  fun tomcatCustomizer(): WebServerFactoryCustomizer<TomcatServletWebServerFactory> =
    WebServerFactoryCustomizer { factory: TomcatServletWebServerFactory ->
      factory.addConnectorCustomizers(
        { connector: Connector ->
          connector.encodedSolidusHandling = EncodedSolidusHandling.PASS_THROUGH.value
        },
      )
    }
}
