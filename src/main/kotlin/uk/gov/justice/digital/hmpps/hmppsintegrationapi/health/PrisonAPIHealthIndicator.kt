package uk.gov.justice.digital.hmpps.hmppsintegrationapi.health

import org.springframework.stereotype.Component

// Dependency injected as it's a bean
// This will add an entry to /health
@Component("prisonapi")
class PrisonAPIHealthIndicator : ExternalHealthIndicator("http://localhost:8081/health/ping")
