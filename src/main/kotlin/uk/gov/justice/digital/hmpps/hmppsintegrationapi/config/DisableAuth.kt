package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class DisableAuth {
  @Bean
  @Throws(Exception::class)
  fun securityFilterChain(http: HttpSecurity, @Autowired objectMapper: ObjectMapper): SecurityFilterChain {
    http
      .csrf { csrf: CsrfConfigurer<HttpSecurity> -> csrf.disable() }
      .authorizeHttpRequests { auth ->
        auth
          .requestMatchers("/health/**").permitAll()
          .requestMatchers("/info").permitAll()
          .anyRequest().permitAll()
      }
      .anonymous { anonymous ->
        anonymous.disable()
      }
    return http.build()
  }
}
