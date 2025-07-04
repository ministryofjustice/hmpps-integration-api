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
            "PersonNotFound",
            Schema<ErrorResponse>().description("Failed to find a person with the provided HMPPS ID.").properties(
              mapOf(
                "status" to Schema<Int>().type("number").example(404),
                "userMessage" to Schema<String>().type("string").example("404 Not found error: Could not find person with HMPPS id: 2003/0011991D."),
                "developerMessage" to Schema<String>().type("string").example("Could not find person with HMPPS id: 2003/0011991D."),
              ),
            ),
          ).addSchemas(
            "PrisonNotFound",
            Schema<ErrorResponse>().description("Failed to find a prison with the provided prison ID.").properties(
              mapOf(
                "status" to Schema<Int>().type("number").example(404),
                "userMessage" to Schema<String>().type("string").example("404 Not found error: Could not find prison with prison ID: MDI."),
                "developerMessage" to Schema<String>().type("string").example("Could not find prison with prison with ID: MDI."),
              ),
            ),
          ).addSchemas(
            "NotFoundError",
            Schema<ErrorResponse>().description("The requested resource could not be found.").properties(
              mapOf(
                "status" to Schema<Int>().type("number").example(404),
                "userMessage" to Schema<String>().type("string").example("404 Not found error: The requested resource could not be found."),
                "developerMessage" to Schema<String>().type("string").example("Resource not found for the given identifier."),
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
          ).addSchemas(
            "ForbiddenResponse",
            Schema<ErrorResponse>().description("Forbidden to complete action by upstream service").properties(
              mapOf(
                "status" to Schema<Int>().type("number").example(403),
                "userMessage" to Schema<String>().type("string").example("Forbidden to complete action by upstream service"),
                "developerMessage" to Schema<String>().type("string").example("Forbidden to complete action by upstream service"),
              ),
            ),
          ).addSchemas(
            "ConflictResponse",
            Schema<ErrorResponse>().description("Action could not be completed due to a conflict").properties(
              mapOf(
                "status" to Schema<Int>().type("integer").example(409),
                "userMessage" to Schema<String>().type("string").example("Action could not be completed because of a conflict with the current resource state."),
                "developerMessage" to Schema<String>().type("string").example("Resource already exists or state transition is not allowed."),
                "conflictField" to Schema<String>().type("string").example("username"),
                "timestamp" to Schema<String>().type("string").example("2025-07-03T10:00:00Z"),
              ),
            ),
          )
      }
    }
}
