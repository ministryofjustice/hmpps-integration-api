package uk.gov.justice.digital.hmpps.hmppsintegrationapi.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

// Dependency injected as it's a bean
// This will add an entry to /health
@Component("hmppsauth")
class HmppsAuthAPIHealthIndicator(@Value("\${services.hmpps-auth.base-url}") baseUrl: String) : ExternalHealthIndicator("$baseUrl/auth/health")
