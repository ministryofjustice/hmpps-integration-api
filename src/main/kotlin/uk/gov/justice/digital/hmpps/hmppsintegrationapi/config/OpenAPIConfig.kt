package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@OpenAPIDefinition(
  info =
    Info(
      title = "HMPPS Integration API",
      description = "A long-lived API that exposes data from HMPPS systems such as the National Offender Management Information System (NOMIS), nDelius (probation system) and Offender Assessment System (OASys), providing a single point of entry for consumers.",
      license =
        License(
          name = "MIT",
          url = "https://github.com/ministryofjustice/hmpps-integration-api/blob/main/LICENSE",
        ),
      version = "1.0",
    ),
  servers = [
    Server(url = "https://hmpps-integration-api-dev.apps.live.cloud-platform.service.justice.gov.uk", description = "Development server"),
    Server(url = "https://hmpps-integration-api-preprod.apps.live.cloud-platform.service.justice.gov.uk", description = "Pre-production server, containing live data"),
    Server(url = "https://hmpps-integration-api-prod.apps.live.cloud-platform.service.justice.gov.uk", description = "Production"),
  ],
  security = [
    SecurityRequirement(name = "mutual-tls"),
    SecurityRequirement(name = "api-key"),
  ],
)
@SecurityScheme(
  name = "mutual-tls",
  type = SecuritySchemeType.MUTUALTLS,
)
@SecurityScheme(
  name = "api-key",
  type = SecuritySchemeType.APIKEY,
  `in` = SecuritySchemeIn.HEADER,
  paramName = "x-api-key",
)
@Configuration
class OpenAPIConfig {
  @Bean
  fun openApiCustomizer(): OpenApiCustomizer =
    object : GlobalOpenApiCustomizer {
      override fun customise(openApi: OpenAPI) {
        openApi.components
          .addSchemas(
            "BadRequest",
            Schema<ErrorResponse>()
              .example(ErrorResponse(400, userMessage = "Validation failure: No query parameters specified.", developerMessage = "No query parameters specified.")),
          )
          .addSchemas(
            "PersonNotFound",
            Schema<ErrorResponse>()
              .description("Failed to find a person with the provided HMPPS ID.")
              .example(ErrorResponse(404, userMessage = "404 Not found error: Could not find person with HMPPS id: 2003/0011991D.", developerMessage = "Could not find person with HMPPS id: 2003/0011991D.")),
          )
          .addSchemas(
            "InternalServerError",
            Schema<ErrorResponse>()
              .description("An upstream service was not responding, so we cannot verify the accuracy of any data we did get.")
              .example(ErrorResponse(500, userMessage = "Internal Server Error", developerMessage = "Unable to complete request as an upstream service is not responding.")),
          )
      }
    }
}
