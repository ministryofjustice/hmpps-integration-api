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
import io.swagger.v3.oas.models.parameters.Parameter
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

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
  companion object {
    const val HMPPS_ID = "#components/parameters/hmppsId"
    const val PAGE = "#components/parameters/page"
    const val PER_PAGE = "#components/parameters/perPage"
  }

  @Bean
  fun openApiCustomizer(): OpenApiCustomizer =
    object : GlobalOpenApiCustomizer {
      override fun customise(openApi: OpenAPI) {
        openApi.components
          .addParameters(
            "hmppsId",
            Parameter()
              .name("hmppsId")
              .description("A URL-encoded HMPPS identifier")
              .example("2008%2F0545166T")
              .schema(Schema<String>().type("string"))
              .`in`("path")
              .required(true),
          )
          .addParameters(
            "page",
            Parameter()
              .name("page")
              .description("The page number (starting from 1)")
              .schema(Schema<Int>().type("number").minimum(BigDecimal.ONE)._default(1))
              .`in`("query")
              .required(false),
          )
          .addParameters(
            "perPage",
            Parameter().name("perPage")
              .description("The maximum number of results for a page")
              .schema(Schema<Int>().type("number").minimum(BigDecimal.ONE)._default(10))
              .`in`("query")
              .required(false),
          )
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
