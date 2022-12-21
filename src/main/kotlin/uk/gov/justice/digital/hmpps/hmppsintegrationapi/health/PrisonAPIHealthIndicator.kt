package uk.gov.justice.digital.hmpps.hmppsintegrationapi.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

// Dependency injected as it's a bean
// This will add an entry to /health
@Component("prisonapi")
class PrisonAPIHealthIndicator(@Value("\${services.prison-api.base-url}") baseUrl: String) : ExternalHealthIndicator("$baseUrl/health/ping")
