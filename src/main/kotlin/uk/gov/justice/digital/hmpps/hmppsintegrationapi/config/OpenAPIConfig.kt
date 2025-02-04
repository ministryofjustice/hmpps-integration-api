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
    Server(url = "https://dev.integration-api.hmpps.service.justice.gov.uk", description = "Development server"),
    Server(url = "https://preprod.integration-api.hmpps.service.justice.gov.uk", description = "Pre-production server, containing live data"),
    Server(url = "https://integration-api.hmpps.service.justice.gov.uk", description = "Production"),
  ],
  security = [
    SecurityRequirement(name = "dn"),
  ],
)
@SecurityScheme(
  name = "dn",
  type = SecuritySchemeType.APIKEY,
  `in` = SecuritySchemeIn.HEADER,
  paramName = "subject-distinguished-name",
  description = "Example: O=test,CN=automated-test-client",
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
            Schema<ErrorResponse>().properties(
              mapOf(
                "status" to Schema<Int>().type("number").example(400),
                "userMessage" to Schema<String>().type("string").example("Validation failure: No query parameters specified."),
                "developerMessage" to Schema<String>().type("string").example("No query parameters specified."),
              ),
            ),
          ).addSchemas(
            "Forbidden",
            Schema<ErrorResponse>().description("Authorisation failed").properties(
              mapOf(
                "status" to Schema<Int>().type("number").example(500),
                "userMessage" to Schema<String>().type("string").example("Authorisation failed on upstream"),
                "developerMessage" to Schema<String>().type("string").example("Authorisation failed on upstream."),
              ),
            ),
          ).addSchemas(
            "PersonNotFound",
            Schema<ErrorResponse>().description("Failed to find a person with the provided HMPPS ID.").properties(
              mapOf(
                "status" to Schema<Int>().type("number").example(404),
                "userMessage" to Schema<String>().type("string").example("404 Not found error: Could not find person with HMPPS id: 2003/0011991D."),
                "developerMessage" to Schema<String>().type("string").example("Could not find person with HMPPS id: 2003/0011991D."),
              ),
            ),
          ).addSchemas(
            "InternalServerError",
            Schema<ErrorResponse>().description("An upstream service was not responding, so we cannot verify the accuracy of any data we did get.").properties(
              mapOf(
                "status" to Schema<Int>().type("number").example(500),
                "userMessage" to Schema<String>().type("string").example("Internal Server Error"),
                "developerMessage" to Schema<String>().type("string").example("Unable to complete request as an upstream service is not responding."),
              ),
            ),
          ).addSchemas(
            "TransactionConflict",
            Schema<ErrorResponse>().description("Duplicate post - The client_unique_ref has been used before").properties(
              mapOf(
                "status" to Schema<Int>().type("number").example(409),
                "userMessage" to Schema<String>().type("string").example("Conflict"),
                "developerMessage" to Schema<String>().type("string").example("Duplicate post - The client_unique_ref has been used before"),
              ),
            ),
          )
      }
    }
}
