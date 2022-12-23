package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration {
  @Bean
  fun customConfiguration(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("https://hmpps-integration-api-development.apps.live.cloud-platform.service.justice.gov.uk")
          .description("Development"),
        Server().url("http://localhost:8080").description("Local"),
      )
    )
    .info(
      Info().title("HMPPS Integration API Documentation")
        .description(
          """
            A long-lived API that exposes data from HMPPS systems such as the National Offender Management Information
            System (NOMIS), nDelius (probation system) and Offender Assessment System (OASys), providing a single point
            of entry for consumers.
          """.trimIndent()
        )
        .license(License().name("MIT").url("https://opensource.org/licenses/MIT"))
    )
}
